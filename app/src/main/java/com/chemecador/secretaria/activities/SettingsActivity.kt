package com.chemecador.secretaria.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.chemecador.secretaria.R
import com.chemecador.secretaria.fragments.SettingsFragment
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val mToolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        // Configurar el botón de retroceso en la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configurar el clic del botón de retroceso
        mToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Cargar el fragmento de preferencias
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Manejar el evento del botón de retroceso en la barra de acción
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}