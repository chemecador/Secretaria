package com.chemecador.secretaria.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityFriendsBinding
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.fragments.AddFriendFragment
import com.chemecador.secretaria.fragments.FriendListFragment
import com.chemecador.secretaria.fragments.FriendRequestFragment
import com.chemecador.secretaria.utils.PreferencesHandler
import com.google.android.material.bottomnavigation.BottomNavigationView

class FriendsActivity : AppCompatActivity() {
    // Resto del código de la actividad
    private var binding: ActivityFriendsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el estado inicial del switcher
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        // Obtener una referencia al ActionBar
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configurar el clic del botón de retroceso
        binding?.toolbar?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setTitle(R.string.friends)
        // Configurar el BottomNavigationView
        val bnv: BottomNavigationView = findViewById(R.id.bnv_friends)


        // Mostrar la lista de amigos como pestaña inicial
        bnv.selectedItemId = R.id.menu_friends


        bnv.setOnItemSelectedListener { item: MenuItem ->
            setupFragment(item)
            item.isChecked = true // Para resaltar el ítem seleccionado
            true
        }

    }

    private fun setupFragment(item: MenuItem) {
        var fragment: Fragment? = null
        when (item.itemId) {
            R.id.menu_friends -> {
                fragment = FriendListFragment()
            }
            R.id.menu_friend_requests -> {
                fragment = FriendRequestFragment()
            }
            R.id.menu_add_friend -> {
                fragment = AddFriendFragment()
            }
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        val addFriendMenuItem = menu.findItem(R.id.add_friend)
        addFriendMenuItem.isVisible = false
        return true
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
                if (PreferencesHandler.isOnline(this)) DB.getInstance(this).deleteAll()
                this.finish()
                startActivity(Intent(this, LoginActivity::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}