package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ItemCityWeatherBinding

class CityWeatherAdapter(
    private val onClick: (CityWeather) -> Unit
) : ListAdapter<CityWeather, CityWeatherAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemCityWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CityWeather) {
            binding.tvCityName.text    = item.name
            binding.tvCityCountry.text = item.country
            binding.tvCityTemp.text    = "${item.temp.toInt()}°C"
            binding.tvCityDesc.text    = item.description.replaceFirstChar { it.uppercase() }
            binding.tvCityHumidity.text = "${item.humidity}%"
            Glide.with(binding.root)
                .load("https://openweathermap.org/img/wn/${item.icon}@2x.png")
                .into(binding.ivCityIcon)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCityWeatherBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CityWeather>() {
            override fun areItemsTheSame(a: CityWeather, b: CityWeather) = a.name == b.name
            override fun areContentsTheSame(a: CityWeather, b: CityWeather) = a == b
        }
    }
}
