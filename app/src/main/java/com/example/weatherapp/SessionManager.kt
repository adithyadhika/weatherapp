package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager mengelola semua data SharedPreferences aplikasi cuaca.
 * Menyimpan preferensi pengguna seperti kota terakhir, satuan suhu, tema,
 * dan riwayat pencarian.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME        = "WeatherAppSession"
        private const val KEY_LAST_CITY    = "last_city"
        private const val KEY_UNIT         = "temp_unit"       // "metric" / "imperial"
        private const val KEY_DARK_MODE    = "dark_mode"
        private const val KEY_NOTIF        = "notification_on"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_SEARCH_HIST  = "search_history"  // disimpan sebagai CSV
    }

    // ─── Kota Terakhir ────────────────────────────────────────────────────────

    fun saveLastCity(city: String) {
        prefs.edit().putString(KEY_LAST_CITY, city).apply()
    }

    fun getLastCity(): String =
        prefs.getString(KEY_LAST_CITY, "Bandung") ?: "Bandung"

    // ─── Satuan Suhu ──────────────────────────────────────────────────────────

    fun saveUnit(unit: String) {
        prefs.edit().putString(KEY_UNIT, unit).apply()
    }

    /** Mengembalikan "metric" (Celsius) atau "imperial" (Fahrenheit) */
    fun getUnit(): String =
        prefs.getString(KEY_UNIT, "metric") ?: "metric"

    fun isCelsius(): Boolean = getUnit() == "metric"

    // ─── Dark Mode ────────────────────────────────────────────────────────────

    fun saveDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)

    // ─── Notifikasi ───────────────────────────────────────────────────────────

    fun saveNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF, enabled).apply()
    }

    fun isNotificationOn(): Boolean = prefs.getBoolean(KEY_NOTIF, true)

    // ─── First Launch ─────────────────────────────────────────────────────────

    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

    fun setLaunched() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    // ─── Riwayat Pencarian (maks 10) ──────────────────────────────────────────

    fun addSearchHistory(city: String) {
        val current = getSearchHistory().toMutableList()
        current.remove(city)          // hapus duplikat
        current.add(0, city)          // tambah ke posisi pertama
        val trimmed = current.take(10)
        prefs.edit().putString(KEY_SEARCH_HIST, trimmed.joinToString(",")).apply()
    }

    fun getSearchHistory(): List<String> {
        val raw = prefs.getString(KEY_SEARCH_HIST, "") ?: ""
        return if (raw.isBlank()) emptyList()
        else raw.split(",").filter { it.isNotBlank() }
    }

    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HIST).apply()
    }

    // ─── Reset Semua ──────────────────────────────────────────────────────────

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
