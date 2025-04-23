@file:Suppress("DEPRECATION")
package com.chemecador.secretaria.ui.view.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.chemecador.secretaria.ui.view.main.MainActivity
import com.chemecador.secretaria.R
import com.chemecador.secretaria.ui.theme.SecretariaTheme
import com.chemecador.secretaria.ui.view.login.screens.LoginScreen
import com.chemecador.secretaria.ui.viewmodel.login.LoginViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {

            SecretariaTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                val isLoading by viewModel.isLoading.collectAsState()
                val authState by viewModel.authState.observeAsState(initial = null)
                val loginError by viewModel.loginError.collectAsState(initial = null)

                val googleSignInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            account?.idToken?.let { idToken ->
                                viewModel.signInWithGoogle(idToken) {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            MainActivity::class.java
                                        )
                                    )
                                    (context as Activity).finish()
                                }
                            }
                        } catch (e: ApiException) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Error al iniciar sesión con Google: ${e.localizedMessage}")
                            }
                        }
                    }
                }

                LaunchedEffect(authState) {
                    if (authState is Resource.Success) {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as Activity).finish()
                    }
                }

                LaunchedEffect(loginError) {
                    loginError?.let { errorMsg ->
                        snackbarHostState.showSnackbar(errorMsg)
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { paddingValues ->
                    LoginScreen(
                        modifier = Modifier.padding(paddingValues),
                        isLoading = isLoading,
                        onLogin = { email, password ->
                            when {
                                !isValidEmail(email) -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Introduce un email válido")
                                    }
                                }

                                password.length < 6 -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("La contraseña debe tener al menos 6 caracteres")
                                    }
                                }

                                else -> {
                                    viewModel.login(email, password) {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                MainActivity::class.java
                                            )
                                        )
                                        (context as Activity).finish()
                                    }
                                }
                            }
                        },
                        onSignup = { email, password ->
                            when {
                                !isValidEmail(email) -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Introduce un email válido")
                                    }
                                }

                                password.length < 6 -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("La contraseña debe tener al menos 6 caracteres")
                                    }
                                }

                                else -> {
                                    viewModel.signup(email, password) {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                MainActivity::class.java
                                            )
                                        )
                                        (context as Activity).finish()
                                    }
                                }
                            }
                        },
                        onGoogleSignIn = {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        onLoginGuest = {
                            viewModel.signInAnonymously()
                        }
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
