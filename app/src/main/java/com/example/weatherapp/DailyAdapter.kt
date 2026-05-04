package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ItemDailyBinding

class DailyAdapter : ListAdapter<DailyItem, DailyAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyItem) {
            binding.tvDayName.text    = item.day
            binding.tvDayDesc.text    = item.description.replaceFirstChar { it.uppercase() }
            binding.tvDayMin.text     = "${item.tempMin.toInt()}°"
            binding.tvDayMax.text     = "${item.tempMax.toInt()}°"
            Glide.with(binding.root)
                .load("https://openweathermap.org/img/wn/${item.icon}@2x.png")
                .into(binding.ivDayIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DailyItem>() {
            override fun areItemsTheSame(a: DailyItem, b: DailyItem) = a.day == b.day
            override fun areContentsTheSame(a: DailyItem, b: DailyItem) = a == b
        }
    }
}
