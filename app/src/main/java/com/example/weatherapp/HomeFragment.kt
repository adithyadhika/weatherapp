package com.example.weatherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by activityViewModels()
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        updateDateTime()

        // Refresh tombol
        binding.btnRefresh.setOnClickListener {
            val city = binding.tvCityName.text.toString().ifBlank { "Bandung" }
            viewModel.loadWeather(city)
        }

        // Tombol favorit (tambah/hapus dari SQLite)
        binding.btnFavorite.setOnClickListener {
            val cityName = binding.tvCityName.text.toString()
            val country  = binding.tvCountry.text.toString()
            if (cityName.isNotBlank()) {
                viewModel.toggleFavorite(cityName, country)
            }
        }

        // Tampilkan info "kota terakhir" dari SharedPreferences
        val lastCity = viewModel.session.getLastCity()
        binding.tvCityName.text = lastCity
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyAdapter { _ -> }
        binding.rvHourly.apply {
            adapter = hourlyAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        dailyAdapter = DailyAdapter()
        binding.rvDaily.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.currentWeather.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading(true)
                is UiState.Success -> {
                    showLoading(false)
                    bindWeatherData(state.data)
                    binding.tvError.visibility = View.GONE
                }
                is UiState.Error -> {
                    showLoading(false)
                    // Tetap tampilkan konten tapi tunjukkan pesan error (bisa offline)
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }

        viewModel.hourly.observe(viewLifecycleOwner) { hourlyAdapter.submitList(it) }
        viewModel.daily.observe(viewLifecycleOwner) { dailyAdapter.submitList(it) }

        // Update ikon favorit berdasarkan status SQLite
        viewModel.isFavorite.observe(viewLifecycleOwner) { fav ->
            binding.btnFavorite.setImageResource(
                if (fav) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }

    private fun bindWeatherData(data: WeatherResponse) {
        binding.tvCityName.text    = data.name
        binding.tvCountry.text     = data.sys.country
        binding.tvTemperature.text = "${data.main.temp.toInt()}°C"
        binding.tvFeelsLike.text   = "Terasa seperti ${data.main.feels_like.toInt()}°C"
        binding.tvCondition.text   = data.weather.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: ""
        binding.tvHumidity.text    = "${data.main.humidity}%"
        binding.tvWind.text        = "${data.wind.speed.toInt()} km/j ${viewModel.getWindDirection(data.wind.deg)}"
        binding.tvClouds.text      = "${data.clouds.all}%"
        binding.tvPressure.text    = "${data.main.pressure} hPa"
        binding.tvSunrise.text     = viewModel.formatTime(data.sys.sunrise)
        binding.tvSunset.text      = viewModel.formatTime(data.sys.sunset)
        binding.tvVisibility.text  = "${data.visibility / 1000} km"
        Glide.with(this)
            .load(viewModel.getIconUrl(data.weather.firstOrNull()?.icon ?: "01d"))
            .into(binding.ivWeatherIcon)
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility  = if (loading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun updateDateTime() {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id"))
        binding.tvDate.text = sdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}