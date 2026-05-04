package com.example.weatherapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repo    = WeatherRepository()
    val session         = SessionManager(application)
    val favRepo         = FavoriteCityRepository(application)

    private val _currentWeather = MutableLiveData<UiState<WeatherResponse>>()
    val currentWeather: LiveData<UiState<WeatherResponse>> = _currentWeather

    private val _hourly = MutableLiveData<List<HourlyItem>>()
    val hourly: LiveData<List<HourlyItem>> = _hourly

    private val _daily = MutableLiveData<List<DailyItem>>()
    val daily: LiveData<List<DailyItem>> = _daily

    private val _searchResults = MutableLiveData<UiState<WeatherResponse>>()
    val searchResults: LiveData<UiState<WeatherResponse>> = _searchResults

    private val _favoriteCities = MutableLiveData<List<CityWeather>>()
    val favoriteCities: LiveData<List<CityWeather>> = _favoriteCities

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _searchHistory = MutableLiveData<List<String>>()
    val searchHistory: LiveData<List<String>> = _searchHistory

    init {
        val lastCity = session.getLastCity()
        loadWeather(lastCity)
        refreshFavoriteCities()
        refreshSearchHistory()
    }

    fun loadWeather(city: String) {
        viewModelScope.launch {
            _currentWeather.value = UiState.Loading
            val weatherResult = repo.getCurrentWeather(city)
            if (weatherResult.isSuccess) {
                val data = weatherResult.getOrThrow()
                _currentWeather.value = UiState.Success(data)
                // ① Simpan kota terakhir ke SharedPreferences
                session.saveLastCity(data.name)
                // ② Simpan ke cache SQLite
                favRepo.saveCache(
                    city        = data.name,
                    country     = data.sys.country,
                    temp        = data.main.temp,
                    description = data.weather.firstOrNull()?.description ?: "",
                    humidity    = data.main.humidity,
                    windSpeed   = data.wind.speed,
                    icon        = data.weather.firstOrNull()?.icon ?: "01d"
                )
                // ③ Update status favorit
                _isFavorite.value = favRepo.isFavorite(data.name)
            } else {
                val cached = favRepo.getCache(city)
                _currentWeather.value = if (cached != null) {
                    UiState.Error("Offline – data terakhir: ${cached.savedAt}")
                } else {
                    UiState.Error(weatherResult.exceptionOrNull()?.message ?: "Gagal memuat cuaca")
                }
            }
            val forecastResult = repo.getForecast(city)
            if (forecastResult.isSuccess) {
                val forecast = forecastResult.getOrThrow()
                _hourly.value = repo.extractHourly(forecast)
                _daily.value  = repo.extractDaily(forecast)
            }
        }
    }

    fun searchCity(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _searchResults.value = UiState.Loading
            val result = repo.getCurrentWeather(query)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                _searchResults.value = UiState.Success(data)
                // Simpan ke riwayat pencarian SharedPreferences
                session.addSearchHistory(data.name)
                refreshSearchHistory()
            } else {
                _searchResults.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Kota tidak ditemukan"
                )
            }
        }
    }

    fun toggleFavorite(cityName: String, country: String) {
        viewModelScope.launch {
            if (favRepo.isFavorite(cityName)) {
                favRepo.removeFavorite(cityName)
                _isFavorite.value = false
            } else {
                favRepo.addFavorite(cityName, country)
                _isFavorite.value = true
            }
            refreshFavoriteCities()
        }
    }

    fun refreshFavoriteCities() {
        viewModelScope.launch {
            val favorites = favRepo.getAllFavorites()
            val cityWeathers = mutableListOf<CityWeather>()
            favorites.forEach { fav ->
                val cached = favRepo.getCache(fav.name)
                if (cached != null) {
                    cityWeathers.add(CityWeather(
                        name = cached.city, country = cached.country,
                        temp = cached.temp, description = cached.description,
                        icon = cached.icon, humidity = cached.humidity, windSpeed = cached.windSpeed
                    ))
                    _favoriteCities.value = cityWeathers.toList()
                }
                val result = repo.getCurrentWeather(fav.name)
                if (result.isSuccess) {
                    val r = result.getOrThrow()
                    val cw = CityWeather(
                        name = r.name, country = r.sys.country, temp = r.main.temp,
                        description = r.weather.firstOrNull()?.description ?: "",
                        icon = r.weather.firstOrNull()?.icon ?: "01d",
                        humidity = r.main.humidity, windSpeed = r.wind.speed
                    )
                    favRepo.saveCache(
                        city = r.name, country = r.sys.country, temp = r.main.temp,
                        description = r.weather.firstOrNull()?.description ?: "",
                        humidity = r.main.humidity, windSpeed = r.wind.speed,
                        icon = r.weather.firstOrNull()?.icon ?: "01d"
                    )
                    val idx = cityWeathers.indexOfFirst { it.name == cw.name }
                    if (idx >= 0) cityWeathers[idx] = cw else cityWeathers.add(cw)
                    _favoriteCities.value = cityWeathers.toList()
                }
            }
        }
    }

    fun refreshSearchHistory() { _searchHistory.value = session.getSearchHistory() }
    fun clearSearchHistory()   { session.clearSearchHistory(); refreshSearchHistory() }

    fun getIconUrl(icon: String)   = repo.getIconUrl(icon)
    fun formatTime(epoch: Long)    = repo.formatTime(epoch)
    fun getWindDirection(deg: Int) = repo.getWindDirection(deg)
}