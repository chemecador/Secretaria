package com.chemecador.secretaria.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityAboutUsBinding
import com.chemecador.secretaria.fragments.AboutUsFragment

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener una referencia al ActionBar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configurar el clic del botón de retroceso
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setTitle(R.string.about_us)

        // Configurar el fragmento de Acerca de nosotros
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AboutUsFragment())
            .commit()
    }
}