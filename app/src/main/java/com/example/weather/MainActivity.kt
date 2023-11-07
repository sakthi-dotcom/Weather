package com.example.weather

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weather.adapter.WeatherToday
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.model.WeatherList
import com.example.weather.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel:WeatherViewModel
    private lateinit var adapter:WeatherToday

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        adapter = WeatherToday()
        binding.lifecycleOwner = this
        binding.vm = viewModel
        viewModel.getWeather()

        // when ever app runs last searched city will disappear
        val sharedPrefs = SharedPrefs.getInstance(this@MainActivity)
        sharedPrefs.clearCityValue()

        viewModel.todayWeatherLiveData.observe(this, Observer {
            val newList = it as List<WeatherList>
            adapter.setList(newList)
            binding.forecastRecyclerView.adapter = adapter
        })
        locationAccess()
        foreCastData()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Utils.PERMISSION_REQUEST_CODE){
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){

                getCurrentLocation()
            }else{
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun foreCastData(){
        viewModel.closetorexactlysameweatherdata.observe(this, Observer {


            val temperatureFahrenheit = it!!.main?.temp
            val temperatureCelsius = (temperatureFahrenheit?.minus(273.15))
            val temperatureFormatted = String.format("%.2f", temperatureCelsius)

            for (i in it.weather) {
                binding.descMain.text = i.description

            }

            binding.tempMain.text = "$temperatureFormatted°C"
            binding.humidityMain.text = it.main!!.humidity.toString()
            binding.windSpeed.text = it.wind?.speed.toString()
            binding.chanceofrain.text = "${it.pop.toString()}%"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(it.dtTxt!!)
            val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
            val dateAndDayName = outputFormat.format(date!!)

            binding.dateDayMain.text = dateAndDayName


            // setting the icon
            for (i in it.weather) {
                if (i.icon == "01d") {
                    binding.imageMain.setImageResource(R.drawable.oned)
                }
                if (i.icon == "01n") {
                    binding.imageMain.setImageResource(R.drawable.onen)
                }
                if (i.icon == "02d") {
                    binding.imageMain.setImageResource(R.drawable.twod)
                }
                if (i.icon == "02n") {
                    binding.imageMain.setImageResource(R.drawable.twon)
                }
                if (i.icon == "03d" || i.icon == "03n") {
                    binding.imageMain.setImageResource(R.drawable.threedn)
                }
                if (i.icon == "10d") {
                    binding.imageMain.setImageResource(R.drawable.tend)
                }
                if (i.icon == "10n") {
                    binding.imageMain.setImageResource(R.drawable.tenn)
                }
                if (i.icon == "04d" || i.icon == "04n") {
                    binding.imageMain.setImageResource(R.drawable.fourdn)
                }
                if (i.icon == "09d" || i.icon == "09n") {
                    binding.imageMain.setImageResource(R.drawable.ninedn)
                }
                if (i.icon == "11d" || i.icon == "11n") {
                    binding.imageMain.setImageResource(R.drawable.elevend)
                }
                if (i.icon == "13d" || i.icon == "13n") {
                    binding.imageMain.setImageResource(R.drawable.thirteend)
                }
                if (i.icon == "50d" || i.icon == "50n") {
                    binding.imageMain.setImageResource(R.drawable.fiftydn)
                }
            }

        })
        searchEdit()
    }

    private fun searchEdit(){
        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.BLACK)


        binding.next5Days.setOnClickListener {
            startActivity(Intent(this, ForeCastActivity::class.java))
        }
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{


            override fun onQueryTextSubmit(query: String?): Boolean {
                val prefs = SharedPrefs.getInstance(this@MainActivity)
                prefs.setValue("city",query!!)
                if (!query.isNullOrEmpty()){
                    viewModel.getWeather(query)
                    binding.searchView.setQuery("",false)
                    binding.searchView.clearFocus()
                    binding.searchView.isIconified = true
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun locationAccess(){

        if (checkLocationPermission()){
            // if permission granted,proceed to get current location
            getCurrentLocation()
        }else{
            // permission not granted request location permission
            requestLocationPermission()
        }

    }

    private fun checkLocationPermission() : Boolean {

        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED

    }

    private fun requestLocationPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            Utils.PERMISSION_REQUEST_CODE
        )

    }



    private fun getCurrentLocation(){

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ){

            val location : Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null){
                val latitude = location.latitude
                val longitude = location.longitude
                val myPrefs = SharedPrefs.getInstance(this@MainActivity)
                myPrefs.setValue("lon",longitude.toString())
                myPrefs.setValue("lat",latitude.toString())

                Log.d("Co-ordinates","Latitude:$latitude, Longitude:$longitude")
            }
        }
    }
}