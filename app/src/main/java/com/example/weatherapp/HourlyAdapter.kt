package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ItemHourlyBinding

class HourlyAdapter(
    private val onClick: (HourlyItem) -> Unit
) : ListAdapter<HourlyItem, HourlyAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemHourlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HourlyItem) {
            binding.tvHourTime.text = item.time
            binding.tvHourTemp.text = "${item.temp.toInt()}°"
            Glide.with(binding.root)
                .load("https://openweathermap.org/img/wn/${item.icon}@2x.png")
                .into(binding.ivHourIcon)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<HourlyItem>() {
            override fun areItemsTheSame(a: HourlyItem, b: HourlyItem) = a.time == b.time
            override fun areContentsTheSame(a: HourlyItem, b: HourlyItem) = a == b
        }
    }
}
