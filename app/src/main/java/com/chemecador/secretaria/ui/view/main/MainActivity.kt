package com.chemecador.secretaria.ui.view.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityMainBinding
import com.chemecador.secretaria.ui.view.settings.SettingsActivity
import com.chemecador.secretaria.ui.viewmodel.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Null binding")
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()


    }


    private fun initUI() {
        initToolbar()
        //initListeners()
        //initUIState()

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Observar cambios en la URL de la foto del usuario
        lifecycleScope.launch {
            mainViewModel.pfpUri.collect { uri ->
                if (uri == null) {
                    Glide.with(this@MainActivity)
                        .load(R.drawable.ic_settings)
                        .placeholder(R.drawable.ic_settings)
                        .error(R.drawable.ic_settings)
                        .into(binding.ivProfile)
                } else {
                    Glide.with(this@MainActivity)
                        .load(uri)
                        .into(binding.ivProfile)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}