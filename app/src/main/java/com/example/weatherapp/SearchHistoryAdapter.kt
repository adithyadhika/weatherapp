package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SearchHistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onClearAll: () -> Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM   = 1

        val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(a: String, b: String) = a == b
            override fun areContentsTheSame(a: String, b: String) = a == b
        }
    }

    // ─── Header ViewHolder ────────────────────────────────────────────────────

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel : TextView = itemView.findViewById(R.id.tvHistoryLabel)
        private val tvClear : TextView = itemView.findViewById(R.id.tvClearHistory)
        fun bind() {
            tvLabel.text = "Pencarian Terakhir"
            tvClear.setOnClickListener { onClearAll() }
        }
    }

    // ─── Item ViewHolder ──────────────────────────────────────────────────────

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCity: TextView = itemView.findViewById(R.id.tvHistoryCity)
        fun bind(city: String) {
            tvCity.text = city
            itemView.setOnClickListener { onItemClick(city) }
        }
    }

    // ─── Adapter overrides ────────────────────────────────────────────────────

    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_HEADER else TYPE_ITEM

    override fun getItemCount(): Int = super.getItemCount() + 1  // +1 untuk header

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_header, parent, false)
            HeaderViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            ItemViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) holder.bind()
        else if (holder is ItemViewHolder) holder.bind(getItem(position - 1))
    }
}
