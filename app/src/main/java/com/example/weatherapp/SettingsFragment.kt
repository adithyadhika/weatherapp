package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.weatherapp.databinding.FragmentSettingsBinding

/**
 * SettingsFragment menampilkan preferensi aplikasi yang disimpan via SharedPreferences:
 *  - Satuan suhu (Celsius / Fahrenheit)
 *  - Dark Mode
 *  - Notifikasi
 *  - Hapus riwayat pencarian
 *  - Info kota terakhir
 *  - Total kota favorit dari SQLite
 *  - Reset semua preferensi
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCurrentSettings()
        setupListeners()
    }

    /** Baca nilai dari SharedPreferences dan tampilkan ke UI */
    private fun loadCurrentSettings() {
        val session = viewModel.session

        // Satuan suhu
        binding.switchCelsius.isChecked = session.isCelsius()
        binding.tvUnitLabel.text =
            if (session.isCelsius()) "Celsius (°C)" else "Fahrenheit (°F)"

        // Dark Mode
        binding.switchDarkMode.isChecked = session.isDarkMode()

        // Notifikasi
        binding.switchNotif.isChecked = session.isNotificationOn()

        // Info kota terakhir (dari SharedPreferences)
        binding.tvLastCity.text = session.getLastCity()

        // Total riwayat pencarian
        val histCount = session.getSearchHistory().size
        binding.tvHistCount.text = "$histCount pencarian tersimpan"

        // Total kota favorit (dari SQLite)
        val favCount = viewModel.favRepo.countFavorites()
        binding.tvFavCount.text = "$favCount kota favorit"
    }

    private fun setupListeners() {
        val session = viewModel.session

        // ── Satuan suhu ────────────────────────────────────────────────────────
        binding.switchCelsius.setOnCheckedChangeListener { _, isChecked ->
            val unit = if (isChecked) "metric" else "imperial"
            session.saveUnit(unit)                          // simpan ke SharedPreferences
            binding.tvUnitLabel.text =
                if (isChecked) "Celsius (°C)" else "Fahrenheit (°F)"
            Toast.makeText(
                requireContext(),
                "Satuan diubah ke ${if (isChecked) "Celsius" else "Fahrenheit"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ── Dark Mode ──────────────────────────────────────────────────────────
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.saveDarkMode(isChecked)                 // simpan ke SharedPreferences
            Toast.makeText(
                requireContext(),
                if (isChecked) "Dark mode aktif" else "Light mode aktif",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ── Notifikasi ────────────────────────────────────────────────────────
        binding.switchNotif.setOnCheckedChangeListener { _, isChecked ->
            session.saveNotification(isChecked)             // simpan ke SharedPreferences
        }

        // ── Hapus riwayat pencarian ───────────────────────────────────────────
        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Riwayat")
                .setMessage("Hapus semua riwayat pencarian?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.clearSearchHistory()
                    binding.tvHistCount.text = "0 pencarian tersimpan"
                    Toast.makeText(requireContext(), "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // ── Hapus semua favorit (SQLite) ──────────────────────────────────────
        binding.btnClearFavorites.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Favorit")
                .setMessage("Hapus semua kota favorit? Tindakan ini tidak bisa dibatalkan.")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.favRepo.clearAllFavorites()
                    viewModel.refreshFavoriteCities()
                    binding.tvFavCount.text = "0 kota favorit"
                    Toast.makeText(requireContext(), "Semua favorit dihapus", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // ── Reset semua preferensi ────────────────────────────────────────────
        binding.btnResetAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Pengaturan")
                .setMessage("Reset semua preferensi ke pengaturan awal?")
                .setPositiveButton("Reset") { _, _ ->
                    session.clearAll()                      // hapus semua SharedPreferences
                    Toast.makeText(requireContext(), "Pengaturan direset", Toast.LENGTH_SHORT).show()
                    // Restart activity agar perubahan tema berlaku
                    val intent = requireActivity().intent
                    requireActivity().finish()
                    startActivity(intent)
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Refresh tampilan saat kembali ke fragment ini
        binding.root.post { loadCurrentSettings() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
