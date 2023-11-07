package com.example.weather.service

import com.example.weather.Utils
import com.example.weather.model.ForeCast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {


    @GET("forecast?")
    fun getCurrentWeather(@Query("lat")lat: String, @Query("lon")lon: String, @Query("appid")appid: String = Utils.API_KEY): Call<ForeCast>

    @GET("forecast?")
    fun getWeatherByCity(
        @Query("q")
        city: String,
        @Query("appid")
        appid: String = Utils.API_KEY

    ): Call<ForeCast>
}