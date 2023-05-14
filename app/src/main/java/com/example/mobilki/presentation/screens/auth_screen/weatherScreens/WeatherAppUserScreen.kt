package com.example.mobilki.presentation.screens.auth_screen.weatherScreens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobilki.presentation.weather.api.WeatherApiClient
import com.example.mobilki.presentation.weather.api.WeatherResponse
import com.example.mobilki.ui.theme.typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.mobilki.presentation.weather.api.Result
import java.text.DecimalFormat
import androidx.compose.foundation.Image


@Composable
fun WeatherAppUserScreen() {
    val weatherResponseState = remember { mutableStateOf<WeatherResponse?>(null) }
    val cityState = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weather App for Users",
            style = typography.h6,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        var city by cityState
        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Enter city") },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Button(
            onClick = {
                val weatherApiClient = WeatherApiClient("d87d2f8e4941f9a0c6dddf490d5780c8")

                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        weatherApiClient.getCurrentWeather(city)
                    }

                    when (result) {
                        is Result.Success -> {
                            weatherResponseState.value = result.data
                        }
                        is Result.Error -> {
                            Log.e("WeatherAppUserScreen", "Error fetching weather: ${result.message}")
                            // Additional error handling or displaying an error message to the user can be done here
                        }
                        else -> {
                            Log.e("WeatherAppUserScreen", "Unexpected result type: $result")
                            // Handle any unexpected result types here
                        }
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Get Weather")
        }


        weatherResponseState.value?.let { weatherResponse ->
            val temperatureInCelsius = weatherResponse.main.temp - 273.15
            val decimalFormat = DecimalFormat("0.00") // Формат с двумя знаками после запятой
            val formattedTemperature = decimalFormat.format(temperatureInCelsius)
            Text(
                text = "Temperature: $formattedTemperature°C",
                modifier = Modifier.padding(top = 16.dp)
            )

//            val iconCode = weatherResponse.weather.firstOrNull()?.icon
//            println(iconCode)
//            if (iconCode != null) {
//                val iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png"
//                println(iconUrl)
//                Image(
//                    painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/04d@4x.png"),
//                    contentDescription = "Weather Icon",
//                    modifier = Modifier.size(200.dp)
//                )
//            }

            val iconCode = weatherResponse.weather.firstOrNull()?.icon
            println(iconCode)
            if (iconCode != null) {
                val resourceName = "ic_weather_$iconCode@2x"
                val resourceId = context.resources.getIdentifier(resourceName, "drawable", packageName)
                val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(200.dp)
                )

            }


        }
    }
}


