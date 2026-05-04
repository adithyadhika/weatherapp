package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * WeatherDatabaseHelper mewarisi SQLiteOpenHelper.
 * Mengelola dua tabel:
 *   1. favorite_cities  → kota yang disimpan pengguna
 *   2. weather_cache    → cache hasil API (offline mode)
 */
class WeatherDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME    = "weather_app.db"
        private const val DB_VERSION = 1

        // ── Tabel 1: Kota Favorit ──────────────────────────────────────────
        const val TABLE_FAVORITES    = "favorite_cities"
        const val COL_FAV_ID         = "id"
        const val COL_FAV_NAME       = "name"
        const val COL_FAV_COUNTRY    = "country"
        const val COL_FAV_ADDED_AT   = "added_at"

        // ── Tabel 2: Cache Cuaca ───────────────────────────────────────────
        const val TABLE_CACHE        = "weather_cache"
        const val COL_CACHE_ID       = "id"
        const val COL_CACHE_CITY     = "city"
        const val COL_CACHE_TEMP     = "temp"
        const val COL_CACHE_DESC     = "description"
        const val COL_CACHE_HUMIDITY = "humidity"
        const val COL_CACHE_WIND     = "wind_speed"
        const val COL_CACHE_ICON     = "icon"
        const val COL_CACHE_COUNTRY  = "country"
        const val COL_CACHE_SAVED_AT = "saved_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Buat tabel kota favorit
        db.execSQL("""
            CREATE TABLE $TABLE_FAVORITES (
                $COL_FAV_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FAV_NAME     TEXT NOT NULL,
                $COL_FAV_COUNTRY  TEXT NOT NULL DEFAULT '',
                $COL_FAV_ADDED_AT TEXT NOT NULL
            )
        """.trimIndent())

        // Buat tabel cache cuaca
        db.execSQL("""
            CREATE TABLE $TABLE_CACHE (
                $COL_CACHE_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CACHE_CITY     TEXT NOT NULL UNIQUE,
                $COL_CACHE_TEMP     REAL NOT NULL DEFAULT 0,
                $COL_CACHE_DESC     TEXT NOT NULL DEFAULT '',
                $COL_CACHE_HUMIDITY INTEGER NOT NULL DEFAULT 0,
                $COL_CACHE_WIND     REAL NOT NULL DEFAULT 0,
                $COL_CACHE_ICON     TEXT NOT NULL DEFAULT '01d',
                $COL_CACHE_COUNTRY  TEXT NOT NULL DEFAULT '',
                $COL_CACHE_SAVED_AT TEXT NOT NULL
            )
        """.trimIndent())

        // Isi 5 kota favorit default (seperti pola insertSampleData pada modul)
        insertDefaultFavorites(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE")
        onCreate(db)
    }

    /** Menyisipkan kota-kota favorit bawaan saat pertama kali DB dibuat */
    private fun insertDefaultFavorites(db: SQLiteDatabase) {
        val now = currentTimestamp()
        val defaults = listOf(
            arrayOf("Bandung",    "ID"),
            arrayOf("Jakarta",    "ID"),
            arrayOf("Surabaya",   "ID"),
            arrayOf("Yogyakarta", "ID"),
            arrayOf("Medan",      "ID")
        )
        defaults.forEach { row ->
            val cv = ContentValues().apply {
                put(COL_FAV_NAME,    row[0])
                put(COL_FAV_COUNTRY, row[1])
                put(COL_FAV_ADDED_AT, now)
            }
            db.insert(TABLE_FAVORITES, null, cv)
        }
    }

    private fun currentTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
