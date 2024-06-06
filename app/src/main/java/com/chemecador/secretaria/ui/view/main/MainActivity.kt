package com.chemecador.secretaria.ui.view.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
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
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()


        // Init fragment
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container, NotesListFragment())
            }
        }

        // Listen for fragment results to update the title
        supportFragmentManager.setFragmentResultListener(TITLE_REQUEST_KEY, this) { _, bundle ->
            val title = bundle.getString(TITLE_KEY)
            supportActionBar?.title = title
        }
    }


    private fun initUI() {
        initToolbar()
        handleOnBackPressed()
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun handleOnBackPressed() {

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        lifecycleScope.launch {
            mainViewModel.pfpUri.collect { uri ->
                if (uri == null) {
                    Glide.with(this@MainActivity)
                        .load(R.drawable.ic_settings_white)
                        .placeholder(R.drawable.ic_settings_white)
                        .error(R.drawable.ic_settings_white)
                        .into(binding.ivProfile)
                } else {
                    Glide.with(this@MainActivity)
                        .load(uri)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TITLE_REQUEST_KEY = "titleRequestKey"
        const val TITLE_KEY = "titleKey"
    }
}