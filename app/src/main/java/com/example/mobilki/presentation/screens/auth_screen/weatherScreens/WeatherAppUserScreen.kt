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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.example.mobilki.weatherApi.ForecastResponse
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


@SuppressLint("DiscouragedApi", "UnrememberedMutableState", "CoroutineCreationDuringComposition",
    "SimpleDateFormat"
)
@Composable
fun WeatherAppUserScreen() {
    val weatherResponseState = remember { mutableStateOf<WeatherResponse?>(null) }
    val cityState = remember { mutableStateOf("") }
    val locationState = remember { mutableStateOf<Location?>(null) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) } //объект для получения текущей геопозиции. создается с помощью LocationServices.getFusedLocationProviderClient(context), где context - это контекст приложения.
    val PERMISSION_REQUEST_CODE = 1001 //это код запроса разрешения. Он используется при запросе разрешения на доступ к местоположению устройства. Значение 1001 просто выбрано в качестве уникального идентификатора для этого запроса разрешения.
    val coroutineScope = rememberCoroutineScope()
    val hourlyForecastState = remember { mutableStateOf<List<WeatherResponse>>(emptyList()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weather App",
            style = typography.h6,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            var city by cityState
            TextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Enter city") },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
            Button(
                onClick = {
                    weatherResponseState.value = null
                    hourlyForecastState.value = emptyList()

                    val weatherApiClient = WeatherApiClient("d87d2f8e4941f9a0c6dddf490d5780c8")

                    coroutineScope.launch {
                        val currentWeatherResult = withContext(Dispatchers.IO) {
                            weatherApiClient.getCurrentWeather(city)
                        }

                        when (currentWeatherResult) {
                            is Result.Success -> {
                                weatherResponseState.value = currentWeatherResult.data
                                val forecastResult = weatherApiClient.getHourlyForecast(city)

                                when (forecastResult) {
                                    is Result.Success -> {
                                        val forecastResponse = forecastResult.data
                                        println(forecastResponse)
                                        hourlyForecastState.value = forecastResponse.hourlyForecasts
                                        println(hourlyForecastState.value)

                                    }
                                    is Result.Error -> {
                                        Log.e(
                                            "WeatherAppUserScreen",
                                            "Ошибка при получении прогноза погоды на 24 часа: ${forecastResult.message}"
                                        )
                                    }
                                }
                            }
                            is Result.Error -> {
                                Log.e(
                                    "WeatherAppUserScreen",
                                    "Ошибка при получении погоды: ${currentWeatherResult.message}"
                                )
                            }
                            else -> {
                                Log.e(
                                    "WeatherAppUserScreen",
                                    "Unexpected result type: $currentWeatherResult"
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Get Weather",
                    style = typography.body1,
                    color = Color.White
                )
            }
        }

        Text("Or")

        Button(
            onClick = {
                weatherResponseState.value = null
                hourlyForecastState.value = emptyList()
                // Проверить разрешение ACCESS_FINE_LOCATION
                if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context)) {
                    // Запросить текущую геопозицию
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            locationState.value = location

                            location?.let { currentLocation ->
                                val weatherApiClient =
                                    WeatherApiClient("d87d2f8e4941f9a0c6dddf490d5780c8")

                                coroutineScope.launch {
                                    val weatherResult = withContext(Dispatchers.IO) {
                                        weatherApiClient.getCurrentWeatherByCoordinates(
                                            currentLocation.latitude,
                                            currentLocation.longitude
                                        )
                                    }

                                    when (weatherResult) {
                                        is Result.Success -> {
                                            weatherResponseState.value = weatherResult.data
                                        }
                                        is Result.Error -> {
                                            Log.e(
                                                "WeatherAppUserScreen",
                                                "Ошибка при получении погоды: ${weatherResult.message}"
                                            )
                                        }
                                        else -> {
                                            Log.e(
                                                "WeatherAppUserScreen",
                                                "Unexpected result type: $weatherResult"
                                            )
                                        }
                                    }

                                    val forecastResult = withContext(Dispatchers.IO) {
                                        weatherApiClient.getHourlyForecastByCoordinates(
                                            currentLocation.latitude,
                                            currentLocation.longitude
                                        )
                                    }

                                    when (forecastResult) {
                                        is Result.Success -> {
                                            hourlyForecastState.value =
                                                forecastResult.data.hourlyForecasts
                                        }
                                        is Result.Error -> {
                                            Log.e(
                                                "WeatherAppUserScreen",
                                                "Ошибка при получении прогноза погоды на 24 часа: ${forecastResult.message}"
                                            )
                                        }
                                        else -> {
                                            Log.e(
                                                "WeatherAppUserScreen",
                                                "Unexpected result type: $forecastResult"
                                            )
                                        }
                                    }
                                }
                            }
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
            Text(
                text = "Get Weather by Current Location",
                style = typography.body1,
                color = Color.White
            )
        }


        weatherResponseState.value?.let { weatherResponse ->
            val temperatureInCelsius = weatherResponse.main.temp - 273.15
            val feelsLikeTemperatureInCelsius = weatherResponse.main.feels_like - 273.15

            val formattedTemperature = temperatureInCelsius.toInt().toString()
            val formattedFeelsLikeTemperature = feelsLikeTemperatureInCelsius.toInt().toString()

            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .wrapContentWidth(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = weatherResponse.name,
                    style = typography.h6,
                    modifier = Modifier.padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val iconCode = weatherResponse.weather.firstOrNull()?.icon
                    if (iconCode != null) {
                        val url = "http://openweathermap.org/img/w/$iconCode.png"

                        val painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current).data(data = url).apply(block = fun ImageRequest.Builder.() {
                                size(100, 100)
                            }).build()
                        )

                        Image(
                            painter = painter,
                            contentDescription = "Weather Icon",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(end = 8.dp)
                        )
                    } else {
                        Text(text = "Image not found", style = typography.body2)
                    }
                }




                Column {

                        Text(
                            text = "$formattedTemperature°C",
                            style = typography.h4,
                            modifier = Modifier.padding(bottom = 4.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Feels ${formattedFeelsLikeTemperature}°C",
                            style = typography.h6,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }


        hourlyForecastState.value?.let { hourlyForecasts ->
            val currentDateTime = LocalDateTime.now()
            val endDateTime = currentDateTime.plusHours(24)
            val filteredForecasts = hourlyForecasts.filter { forecast ->
                val forecastDateTime =
                    Instant.ofEpochSecond(forecast.dt).atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                forecastDateTime in currentDateTime..endDateTime
            }

            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(filteredForecasts) { forecast ->
                    val forecastTemperature = forecast.main.temp - 273.15
                    val formattedForecastTemperature = forecastTemperature.toInt().toString()
                    val forecastIconCode = forecast.weather.firstOrNull()?.icon
                    val timestamp = forecast.dt
                    val dateTime =
                        Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault())
                            .toLocalDateTime()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "$dateTime",
                            style = typography.body2,
                            modifier = Modifier.width(85.dp)
                        )

                        forecastIconCode?.let { iconCode ->
                            val url = "http://openweathermap.org/img/w/$iconCode.png"

                            val painter = rememberAsyncImagePainter(model = url)

                            Image(
                                painter = painter,
                                contentDescription = "Weather Icon",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(end = 16.dp)
                            )
                        }


                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Temp: $formattedForecastTemperature°C",
                                style = typography.body2
                            )
                            Text(
                                text = "Humidity: ${forecast.main.humidity}%",
                                style = typography.body2
                            )
                        }
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

