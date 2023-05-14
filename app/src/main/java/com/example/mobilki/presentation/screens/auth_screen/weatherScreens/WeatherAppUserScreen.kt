package com.example.mobilki.presentation.screens.auth_screen.weatherScreens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobilki.weatherApi.WeatherApiClient
import com.example.mobilki.weatherApi.WeatherResponse
import com.example.mobilki.ui.theme.typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.mobilki.weatherApi.Result
import java.text.DecimalFormat
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import com.google.android.gms.location.LocationServices


@SuppressLint("DiscouragedApi", "UnrememberedMutableState", "CoroutineCreationDuringComposition")
@Composable
fun WeatherAppUserScreen() {
    val weatherResponseState = remember { mutableStateOf<WeatherResponse?>(null) }
    val cityState = remember { mutableStateOf("") }

    val locationState = remember { mutableStateOf<Location?>(null) }


    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    val PERMISSION_REQUEST_CODE = 1001




    val coroutineScope = rememberCoroutineScope()


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weather App",
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
                            Log.e(
                                "WeatherAppUserScreen",
                                "Ошибка при получении погоды: ${result.message}"
                            )
                        }
                        else -> {
                            Log.e("WeatherAppUserScreen", "Unexpected result type: $result")
                        }
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Get Weather")
        }

        Button(
            onClick = {
                // Проверить разрешение ACCESS_FINE_LOCATION
                if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context)) {
                    // Запросить текущую геопозицию
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            locationState.value = location
                        }
                        .addOnFailureListener { exception: Exception ->
                            // Обработка ошибки при получении геопозиции
                            Log.e(
                                "WeatherAppUserScreen",
                                "Ошибка при получении геопозиции: ${exception.message}"
                            )
                        }
                } else {
                    // Запросить разрешение у пользователя
                    requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        PERMISSION_REQUEST_CODE,
                        context
                    )
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Get Current Location")
        }

        val location = locationState.value
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            println(latitude)
            println(latitude)


            val weatherApiClient = WeatherApiClient("d87d2f8e4941f9a0c6dddf490d5780c8")

            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    weatherApiClient.getCurrentWeatherByCoordinates(latitude, longitude)
                }
                when (result) {
                    is Result.Success -> {
                        weatherResponseState.value = result.data
                    }
                    is Result.Error -> {
                        Log.e(
                            "WeatherAppUserScreen",
                            "Ошибка при получении погоды: ${result.message}"
                        )
                        // Additional error handling or displaying an error message to the user can be done here
                    }
                    else -> {
                        Log.e("WeatherAppUserScreen", "Unexpected result type: $result")
                        // Handle any unexpected result types here
                    }
                }
            }
        }

        weatherResponseState.value?.let { weatherResponse ->
            val temperatureInCelsius = weatherResponse.main.temp - 273.15
            val decimalFormat = DecimalFormat("0.00") // Формат с двумя знаками после запятой
            val formattedTemperature = decimalFormat.format(temperatureInCelsius)
            Text(
                text = "City: ${weatherResponse.name}",
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Temperature: $formattedTemperature°C",
                modifier = Modifier.padding(top = 16.dp)
            )

            val iconCode = weatherResponse.weather.firstOrNull()?.icon
            println(iconCode)
            if (iconCode != null) {
                val resourceName = "ic_launcher_$iconCode"
                println(resourceName)
                val resourceId = with(context) {
                    resources.getIdentifier(resourceName, "drawable", packageName)
                }
                println(resourceId)

                if (resourceId != 0) {
                    Image(
                        painter = painterResource(resourceId),
                        contentDescription = "Weather Icon"
                    )
                } else {
                    Text(text = "Image not found")
                }
            }
        }
    }
}

private fun checkPermission(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun requestPermission(permission: String, requestCode: Int, context: Context) {
    ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), requestCode)
}


