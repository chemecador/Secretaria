package com.chemecador.secretaria.ui.view.main

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.Constants.TITLE_KEY
import com.chemecador.secretaria.core.constants.Constants.TITLE_REQUEST_KEY
import com.chemecador.secretaria.databinding.ActivityMainBinding
import com.chemecador.secretaria.databinding.DialogLoginRequiredBinding
import com.chemecador.secretaria.ui.view.friends.FriendsActivity
import com.chemecador.secretaria.ui.view.login.LoginActivity
import com.chemecador.secretaria.ui.view.settings.AboutUsActivity
import com.chemecador.secretaria.ui.view.settings.SettingsActivity
import com.chemecador.secretaria.ui.viewmodel.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        lifecycleScope.launch {
            mainViewModel.themeMode.collect { mode ->
                applyTheme(mode)
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
        binding.ivFriends.setOnClickListener {
            handleIvFriendsPressed()
        }
        binding.ivMore.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun handleIvFriendsPressed() {
        if (mainViewModel.isAnonymousUser()) {
            val dialogBinding = DialogLoginRequiredBinding.inflate(LayoutInflater.from(this))

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()
            dialogBinding.btnOk.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                dialog.dismiss()
            }
            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        } else {
            startActivity(Intent(this, FriendsActivity::class.java))
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about_us -> {
                    startActivity(Intent(this, AboutUsActivity::class.java))
                    true
                }

                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }

        popupMenu.setOnDismissListener {
            val menuItems = arrayOf(R.id.action_about_us, R.id.action_settings)
            val isDarkMode = resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val textColor = if (isDarkMode) Color.WHITE else Color.BLACK

            for (menuItemId in menuItems) {
                val menuItem = popupMenu.menu.findItem(menuItemId)
                val spannableTitle = SpannableString(menuItem.title)
                spannableTitle.setSpan(ForegroundColorSpan(textColor), 0, spannableTitle.length, 0)
                menuItem.title = spannableTitle
            }
        }

        popupMenu.show()
    }

    private fun applyTheme(mode: String) {
        val themeValues = resources.getStringArray(R.array.theme_values)
        when (mode) {
            themeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) // "system"
            themeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // "light"
            themeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // "dark"
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
}