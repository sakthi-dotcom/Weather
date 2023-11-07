package com.example.weather

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.ForeCastAdapter
import com.example.weather.model.WeatherList
import com.example.weather.viewmodel.WeatherViewModel

class ForeCastActivity : AppCompatActivity() {

//   lateinit var binding: ActivityForeCastBinding
    private lateinit var adapterForeCastAdapter:ForeCastAdapter
    lateinit var viewModel:WeatherViewModel
    lateinit var forecastRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fore_cast)
        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        adapterForeCastAdapter = ForeCastAdapter()
        forecastRecyclerView = findViewById<RecyclerView>(R.id.rvForeCast)
        foreCast()

    }


     private fun foreCast(){
        val sharedPrefs = SharedPrefs.getInstance(this)
        val city = sharedPrefs.getValue("city")

        if (city != null){
            viewModel.getUpcomingForeCast(city)
        }else{
            viewModel.getUpcomingForeCast()
        }

        viewModel.forecastWeatherLiveData.observe(this, Observer {
            val newList = it as List<WeatherList>
            Log.d("ForeCast Live data",newList.toString())
            adapterForeCastAdapter.setList(newList)
            forecastRecyclerView.adapter = adapterForeCastAdapter
        })
    }
}