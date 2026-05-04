package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by activityViewModels()
    private lateinit var cityAdapter: CityWeatherAdapter
    private lateinit var historyAdapter: SearchHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Adapter kota favorit (dari SQLite)
        cityAdapter = CityWeatherAdapter { city ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("CITY_NAME", city.name)
            }
            startActivity(intent)
        }
        binding.rvCities.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Adapter riwayat pencarian (dari SharedPreferences)
        historyAdapter = SearchHistoryAdapter(
            onItemClick = { cityName ->
                binding.etSearch.setText(cityName)
                viewModel.searchCity(cityName)
            },
            onClearAll = {
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Riwayat")
                    .setMessage("Hapus semua riwayat pencarian?")
                    .setPositiveButton("Hapus") { _, _ ->
                        viewModel.clearSearchHistory()
                        Toast.makeText(requireContext(), "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener { doSearch() }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false
        }
    }

    private fun doSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isNotEmpty()) viewModel.searchCity(query)
    }

    private fun observeViewModel() {
        // Daftar favorit dari SQLite
        viewModel.favoriteCities.observe(viewLifecycleOwner) { list ->
            cityAdapter.submitList(list)
            binding.tvFavCount.text = "Favorit (${list.size})"
        }

        // Riwayat pencarian dari SharedPreferences
        viewModel.searchHistory.observe(viewLifecycleOwner) { history ->
            if (history.isEmpty()) {
                binding.layoutHistory.visibility = View.GONE
            } else {
                binding.layoutHistory.visibility = View.VISIBLE
                historyAdapter.submitList(history)
            }
        }

        // Hasil search → buka Detail
        viewModel.searchResults.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.searchProgress.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.searchProgress.visibility = View.GONE
                    val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                        putExtra("CITY_NAME", state.data.name)
                    }
                    startActivity(intent)
                }
                is UiState.Error -> {
                    binding.searchProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}