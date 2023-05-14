package com.example.mobilki.presentation.weather.api

import com.example.mobilki.R
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherApiClient(private val apiKey: String) {
    private val apiService: WeatherApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(WeatherApiService::class.java)
    }

//    suspend fun getCurrentWeather(city: String): WeatherResponse {
//        return apiService.getCurrentWeather(city, apiKey)
//    }

    suspend fun getCurrentWeather(city: String): Result<WeatherResponse> {
        return try {
            val weatherResponse = apiService.getCurrentWeather(city, apiKey)
            Result.Success(weatherResponse)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }





}
