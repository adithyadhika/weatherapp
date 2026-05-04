package com.example.weatherapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ─── Data Models ──────────────────────────────────────────────────────────────

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int,
    val sys: Sys,
    val clouds: Clouds,
    val coord: Coord
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Sys(
    val sunrise: Long,
    val sunset: Long,
    val country: String
)

data class Clouds(
    val all: Int
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class City(
    val name: String,
    val country: String
)

// Model lokal untuk tampilan
data class CityWeather(
    val name: String,
    val country: String,
    val temp: Double,
    val description: String,
    val icon: String,
    val humidity: Int,
    val windSpeed: Double
)

data class HourlyItem(
    val time: String,
    val temp: Double,
    val icon: String
)

data class DailyItem(
    val day: String,
    val tempMin: Double,
    val tempMax: Double,
    val icon: String,
    val description: String
)

// ─── API Interface ─────────────────────────────────────────────────────────────

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = WeatherApi.API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "id"
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String = WeatherApi.API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "id"
    ): Response<ForecastResponse>
}

// ─── Retrofit Instance ─────────────────────────────────────────────────────────

object WeatherApi {
    // Daftar ke https://openweathermap.org/api dan ganti dengan API key kamu
    const val API_KEY = "469517fb0b75acf182955100b7061284"
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
