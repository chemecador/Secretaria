package com.chemecador.secretaria.ui.view.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chemecador.secretaria.ui.view.main.MainActivity
import com.chemecador.secretaria.ui.viewmodel.login.Route
import com.chemecador.secretaria.ui.viewmodel.login.LauncherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (viewModel.checkRoute()) {
            Route.Main -> startActivity(Intent(this, MainActivity::class.java))
            Route.Login -> startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}