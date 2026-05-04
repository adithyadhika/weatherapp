package com.example.weatherapp

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository {

    private val api = WeatherApi.service

    suspend fun getCurrentWeather(city: String): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeather(city)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Kota tidak ditemukan: $city"))
            }
        } catch (e: Exception) {
            Log.e("WeatherRepo", "Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getForecast(city: String): Result<ForecastResponse> {
        return try {
            val response = api.getForecast(city)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat prakiraan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ambil 8 item pertama untuk tampilan per-jam (tiap 3 jam = 24 jam ke depan)
    fun extractHourly(forecast: ForecastResponse): List<HourlyItem> {
        return forecast.list.take(8).map { item ->
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(item.dt * 1000)
            HourlyItem(
                time = sdf.format(date),
                temp = item.main.temp,
                icon = item.weather.firstOrNull()?.icon ?: "01d"
            )
        }
    }

    // Ambil 1 data per hari untuk prakiraan mingguan
    fun extractDaily(forecast: ForecastResponse): List<DailyItem> {
        val dayMap = LinkedHashMap<String, MutableList<ForecastItem>>()
        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale("id"))

        forecast.list.forEach { item ->
            val key = sdfKey.format(Date(item.dt * 1000))
            dayMap.getOrPut(key) { mutableListOf() }.add(item)
        }

        return dayMap.entries.take(7).map { (dateStr, items) ->
            val date = sdfKey.parse(dateStr) ?: Date()
            val dayName = if (dateStr == sdfKey.format(Date())) "Hari ini"
                         else sdfDay.format(date).replaceFirstChar { it.uppercase() }
            DailyItem(
                day = dayName,
                tempMin = items.minOf { it.main.temp_min },
                tempMax = items.maxOf { it.main.temp_max },
                icon = items[items.size / 2].weather.firstOrNull()?.icon ?: "01d",
                description = items[items.size / 2].weather.firstOrNull()?.description ?: ""
            )
        }
    }

    fun getIconUrl(icon: String) = "https://openweathermap.org/img/wn/${icon}@2x.png"

    fun formatTime(epochSec: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(epochSec * 1000))
    }

    fun getWindDirection(deg: Int): String {
        val directions = arrayOf("U","TL","T","TG","S","BD","B","BL")
        return directions[((deg / 45.0).toInt() % 8)]
    }

    fun getUvIndex(): Int = (1..11).random() // placeholder – UV butuh API berbeda
}
