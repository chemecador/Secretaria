package com.chemecador.secretaria.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityMainBinding
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.fragments.CalendarFragment
import com.chemecador.secretaria.fragments.ListsFragment
import com.chemecador.secretaria.ui.login.LoginActivity
import com.chemecador.secretaria.utils.PreferencesHandler
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var calendarFragment: CalendarFragment? = null
    private var listsFragment: ListsFragment? = null
    private var prefs: SharedPreferences? = null
    private var mode: String? = null
    private val navItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.menu_lists) {
                mode = LIST
                switchState()
                return@OnItemSelectedListener true
            } else if (item.itemId == R.id.menu_calendar) {
                mode = CALENDAR
                switchState()
                return@OnItemSelectedListener true
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el estado inicial del switcher
        binding = ActivityMainBinding.inflate(layoutInflater)
        calendarFragment = CalendarFragment()
        listsFragment = ListsFragment()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        mode = prefs!!.getString("mode", "list")
        init()
    }

    private fun init() {
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        binding!!.bnv.setOnItemSelectedListener(navItemSelectedListener)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)

        // Configurar el clic del botón de retroceso
        binding!!.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        switchState()
        binding!!.bnv.selectedItemId = if (mode == CALENDAR) R.id.menu_calendar else R.id.menu_lists
    }

    private fun switchState() {
        var fragment: Fragment? = null
        when (mode) {
            CALENDAR -> fragment = calendarFragment
            LIST -> fragment = listsFragment
        }
        prefs!!.edit().putString("mode", mode).apply()
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.add_friend -> {
                startActivity(Intent(this, FriendsActivity::class.java))
                return true
            }
            R.id.about_us -> {
                startActivity(Intent(this, AboutUsActivity::class.java))
                return true
            }
            R.id.logout -> {
                if (PreferencesHandler.isOnline(this)) {
                    DB.getInstance(this)!!.deleteAll()
                }
                finish()
                startActivity(Intent(this, LoginActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        val addFriendMenuItem = menu.findItem(R.id.add_friend)
        addFriendMenuItem.isEnabled = PreferencesHandler.isOnline(this)
        return true
    }

    companion object {
        const val CALENDAR = "calendar"
        const val LIST = "list"
    }
}