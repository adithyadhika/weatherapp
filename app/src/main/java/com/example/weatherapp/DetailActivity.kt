package com.example.weatherapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter

    private var currentCityName = ""
    private var currentCountry  = ""
    private var isFav           = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cityName = intent.getStringExtra("CITY_NAME") ?: "Bandung"
        currentCityName = cityName

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        viewModel.loadWeather(cityName)

        // Simpan kota yang dibuka ke SharedPreferences (riwayat kunjungan)
        viewModel.session.saveLastCity(cityName)

        setupToolbar(cityName)
        setupRecyclerViews()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_favorite)?.setIcon(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_favorite) {
            viewModel.toggleFavorite(currentCityName, currentCountry)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar(city: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = city
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyAdapter { _ -> }
        binding.rvHourly.apply {
            adapter = hourlyAdapter
            layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        dailyAdapter = DailyAdapter()
        binding.rvDaily.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(this@DetailActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.currentWeather.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    bindData(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.hourly.observe(this) { hourlyAdapter.submitList(it) }
        viewModel.daily.observe(this)  { dailyAdapter.submitList(it) }

        viewModel.isFavorite.observe(this) { fav ->
            isFav = fav
            invalidateOptionsMenu()
        }
    }

    private fun bindData(data: WeatherResponse) {
        currentCountry = data.sys.country
        binding.tvDetailTemp.text     = "${data.main.temp.toInt()}°C"
        binding.tvDetailCondition.text = data.weather.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: ""
        binding.tvDetailFeels.text    = "Terasa seperti ${data.main.feels_like.toInt()}°C"
        Glide.with(this)
            .load(viewModel.getIconUrl(data.weather.firstOrNull()?.icon ?: "01d"))
            .into(binding.ivDetailIcon)
        binding.tvDetailHumidity.text   = "${data.main.humidity}%"
        binding.tvDetailWind.text       = "${data.wind.speed.toInt()} km/j"
        binding.tvDetailWindDir.text    = viewModel.getWindDirection(data.wind.deg)
        binding.tvDetailPressure.text   = "${data.main.pressure} hPa"
        binding.tvDetailVisibility.text = "${data.visibility / 1000} km"
        binding.tvDetailClouds.text     = "${data.clouds.all}%"
        binding.tvDetailSunrise.text    = viewModel.formatTime(data.sys.sunrise)
        binding.tvDetailSunset.text     = viewModel.formatTime(data.sys.sunset)
        binding.tvDetailTempMin.text    = "Min: ${data.main.temp_min.toInt()}°C"
        binding.tvDetailTempMax.text    = "Maks: ${data.main.temp_max.toInt()}°C"
    }
}