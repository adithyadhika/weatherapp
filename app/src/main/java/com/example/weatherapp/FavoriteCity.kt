package com.example.weatherapp

/**
 * Model data untuk satu kota favorit yang disimpan di SQLite.
 */
data class FavoriteCity(
    val id: Int = 0,
    val name: String,
    val country: String,
    val addedAt: String       // tanggal ditambahkan, format: "yyyy-MM-dd HH:mm"
)
