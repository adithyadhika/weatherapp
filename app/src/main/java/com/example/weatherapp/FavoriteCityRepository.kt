package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class FavoriteCityRepository(context: Context) {

    private val dbHelper = WeatherDatabaseHelper(context)

    // ══════════════════════════════════════════════════════════════════════════
    // FAVORITE CITIES
    // ══════════════════════════════════════════════════════════════════════════

    fun addFavorite(name: String, country: String): Long {
        val cv = ContentValues().apply {
            put(WeatherDatabaseHelper.COL_FAV_NAME,     name)
            put(WeatherDatabaseHelper.COL_FAV_COUNTRY,  country)
            put(WeatherDatabaseHelper.COL_FAV_ADDED_AT, timestamp())
        }
        return dbHelper.writableDatabase.insert(WeatherDatabaseHelper.TABLE_FAVORITES, null, cv)
    }

    fun getAllFavorites(): List<FavoriteCity> {
        val list = mutableListOf<FavoriteCity>()
        val cursor = dbHelper.readableDatabase.query(
            WeatherDatabaseHelper.TABLE_FAVORITES,
            null, null, null, null, null,
            "${WeatherDatabaseHelper.COL_FAV_NAME} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    FavoriteCity(
                        id      = it.getInt(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_FAV_ID)),
                        name    = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_FAV_NAME)),
                        country = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_FAV_COUNTRY)),
                        addedAt = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_FAV_ADDED_AT))
                    )
                )
            }
        }
        return list
    }

    fun isFavorite(name: String): Boolean {
        val cursor = dbHelper.readableDatabase.query(
            WeatherDatabaseHelper.TABLE_FAVORITES,
            arrayOf(WeatherDatabaseHelper.COL_FAV_ID),
            "${WeatherDatabaseHelper.COL_FAV_NAME} = ?",
            arrayOf(name), null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun removeFavorite(name: String): Int {
        return dbHelper.writableDatabase.delete(
            WeatherDatabaseHelper.TABLE_FAVORITES,
            "${WeatherDatabaseHelper.COL_FAV_NAME} = ?",
            arrayOf(name)
        )
    }

    fun clearAllFavorites(): Int {
        return dbHelper.writableDatabase.delete(
            WeatherDatabaseHelper.TABLE_FAVORITES,
            null, null
        )
    }

    fun countFavorites(): Int {
        val cursor = dbHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${WeatherDatabaseHelper.TABLE_FAVORITES}", null
        )
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // WEATHER CACHE
    // ══════════════════════════════════════════════════════════════════════════

    fun saveCache(
        city: String,
        country: String,
        temp: Double,
        description: String,
        humidity: Int,
        windSpeed: Double,
        icon: String
    ) {
        val cv = ContentValues().apply {
            put(WeatherDatabaseHelper.COL_CACHE_CITY,     city)
            put(WeatherDatabaseHelper.COL_CACHE_COUNTRY,  country)
            put(WeatherDatabaseHelper.COL_CACHE_TEMP,     temp)
            put(WeatherDatabaseHelper.COL_CACHE_DESC,     description)
            put(WeatherDatabaseHelper.COL_CACHE_HUMIDITY, humidity)
            put(WeatherDatabaseHelper.COL_CACHE_WIND,     windSpeed)
            put(WeatherDatabaseHelper.COL_CACHE_ICON,     icon)
            put(WeatherDatabaseHelper.COL_CACHE_SAVED_AT, timestamp())
        }
        dbHelper.writableDatabase.insertWithOnConflict(
            WeatherDatabaseHelper.TABLE_CACHE,
            null,
            cv,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getCache(city: String): CachedWeather? {
        val cursor = dbHelper.readableDatabase.query(
            WeatherDatabaseHelper.TABLE_CACHE,
            null,
            "${WeatherDatabaseHelper.COL_CACHE_CITY} = ?",
            arrayOf(city), null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) CachedWeather(
                city        = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_CITY)),
                country     = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_COUNTRY)),
                temp        = it.getDouble(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_TEMP)),
                description = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_DESC)),
                humidity    = it.getInt(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_HUMIDITY)),
                windSpeed   = it.getDouble(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_WIND)),
                icon        = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_ICON)),
                savedAt     = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_SAVED_AT))
            ) else null
        }
    }

    fun getAllCache(): List<CachedWeather> {
        val list = mutableListOf<CachedWeather>()
        val cursor = dbHelper.readableDatabase.query(
            WeatherDatabaseHelper.TABLE_CACHE,
            null, null, null, null, null,
            "${WeatherDatabaseHelper.COL_CACHE_CITY} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CachedWeather(
                        city        = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_CITY)),
                        country     = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_COUNTRY)),
                        temp        = it.getDouble(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_TEMP)),
                        description = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_DESC)),
                        humidity    = it.getInt(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_HUMIDITY)),
                        windSpeed   = it.getDouble(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_WIND)),
                        icon        = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_ICON)),
                        savedAt     = it.getString(it.getColumnIndexOrThrow(WeatherDatabaseHelper.COL_CACHE_SAVED_AT))
                    )
                )
            }
        }
        return list
    }

    private fun timestamp(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}

data class CachedWeather(
    val city: String,
    val country: String,
    val temp: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String,
    val savedAt: String
)