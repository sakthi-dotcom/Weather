package com.example.weather.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.MyApplication
import com.example.weather.SharedPrefs
import com.example.weather.model.WeatherList
import com.example.weather.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherViewModel : ViewModel() {

    val todayWeatherLiveData = MutableLiveData<List<WeatherList>>()
    val forecastWeatherLiveData = MutableLiveData<List<WeatherList>>()

    val closetorexactlysameweatherdata = MutableLiveData<WeatherList?>()
    val cityName = MutableLiveData<String>()
    val context = MyApplication.instance



    fun getWeather(city:String? = null) = viewModelScope.launch (Dispatchers.IO){
        val todayWeatherList = mutableListOf<WeatherList>()
        val currentDateTime = LocalDateTime.now()
        val currentDatePattern = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sharedPrefs = SharedPrefs.getInstance(context)
        val lat = sharedPrefs.getValue("lat").toString()
        val lon = sharedPrefs.getValue("lon").toString()

        Log.d("Co-o VM","$lat $lon")

        val call = if (city != null){
            RetrofitInstance.api.getWeatherByCity(city)
        }else{
            RetrofitInstance.api.getCurrentWeather(lat,lon)
        }

        val response = call.execute()
        if (response.isSuccessful){

            val weatherList = response.body()?.weatherList
            cityName.postValue(response.body()?.city!!.name)
            val presentDate = currentDatePattern

            weatherList?.forEach{ weather ->
                if (weather.dtTxt!!.split("\\s".toRegex()).contains(presentDate)) {
                   todayWeatherList.add(weather)
                }
            }
            val closeWeather = findClosestWeather(todayWeatherList)
            closetorexactlysameweatherdata.postValue(closeWeather)
            todayWeatherLiveData.postValue(todayWeatherList)
        }
    }


    fun getUpcomingForeCast(city:String? = null) = viewModelScope.launch (Dispatchers.IO){
        val foreCastWeatherList = mutableListOf<WeatherList>()
        val currentDateTime = LocalDateTime.now()
        val currentDatePattern = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sharedPrefs = SharedPrefs(context)
        val lat = sharedPrefs.getValue("lat").toString()
        val lon = sharedPrefs.getValue("lon").toString()

        val call = if (city != null){
            RetrofitInstance.api.getWeatherByCity(city)
        }else{
            RetrofitInstance.api.getCurrentWeather(lat,lon)
        }

        val response = call.execute()
        if (response.isSuccessful){

            val weatherList = response.body()?.weatherList
            cityName.postValue(response.body()?.city!!.name)

            val presentDate = currentDatePattern

            weatherList?.forEach{ weather ->
                if (!weather.dtTxt!!.split("\\s".toRegex()).contains(presentDate)) {
                    if (weather.dtTxt!!.substring(11,16) == "12:00"){
                        foreCastWeatherList.add(weather)
                    }

                }
            }
            forecastWeatherLiveData.postValue(foreCastWeatherList)
        }
    }


    private fun findClosestWeather(weatherList: List<WeatherList>): WeatherList? {
        val systemTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        var closestWeather: WeatherList? = null
        var minTimeDifference = Int.MAX_VALUE

        for (weather in weatherList) {
            val weatherTime = weather.dtTxt!!.substring(11, 16)
            val timeDifference = Math.abs(timeToMinutes(weatherTime) - timeToMinutes(systemTime))

            if (timeDifference < minTimeDifference) {
                minTimeDifference = timeDifference
                closestWeather = weather
            }
        }

        return closestWeather
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}