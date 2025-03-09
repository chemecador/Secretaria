package com.chemecador.secretaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chemecador.secretaria.ui.theme.SecretariaTheme
import com.chemecador.secretaria.ui.view.login.main.screens.NotesListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecretariaTheme {
                NotesListScreen()
            }
        }
    }
}
