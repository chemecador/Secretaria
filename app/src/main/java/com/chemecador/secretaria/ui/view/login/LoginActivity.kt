@file:Suppress("DEPRECATION")

package com.chemecador.secretaria.ui.view.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityLoginBinding
import com.chemecador.secretaria.ui.view.main.MainActivity
import com.chemecador.secretaria.ui.viewmodel.login.LoginViewModel
import com.chemecador.secretaria.ui.viewmodel.login.SignupViewmodel
import com.chemecador.secretaria.utils.DeviceUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()
    private val signupViewmodel: SignupViewmodel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initListeners()
        initUIState()
        initGoogle()
    }

    private fun initGoogle() {


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de que este es el ID de cliente correcto
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    loginViewModel.signInWithGoogle(account.idToken!!) {
                        finish()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                } catch (e: ApiException) {
                    Snackbar.make(binding.root, getString(R.string.error_login) + " " + e.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
        binding.btnSignup.setOnClickListener {
            handleSingup()
        }
        binding.btnGoogle.setOnClickListener {
            handleGoogleSignIn()
        }
    }

    private fun handleGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSingup() {
        if (!isEmailValid()) {
            binding.etUsername.requestFocus()
            binding.etUsername.error = getString(R.string.error_email_invalid)
            return
        }
        if (!isPasswordValid()) {
            binding.etPassword.requestFocus()
            binding.etPassword.error = getString(R.string.error_password_invalid)
            return
        }
        DeviceUtils.hideKeyboard(this)
        signupViewmodel.signup(
            user = binding.etUsername.text.toString().trim(),
            password = binding.etPassword.text.toString().trim()
        ) {
            finish()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }
    }

    private fun handleLogin() {
        if (!isEmailValid()) {
            binding.etUsername.requestFocus()
            binding.etUsername.error = getString(R.string.error_email_invalid)
            return
        }
        if (!isPasswordValid()) {
            binding.etPassword.requestFocus()
            binding.etPassword.error = getString(R.string.error_password_invalid)
            return
        }
        DeviceUtils.hideKeyboard(this)
        loginViewModel.login(
            user = binding.etUsername.text.toString().trim(),
            password = binding.etPassword.text.toString().trim()
        ) {
            finish()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    loginViewModel.isLoading.collect {
                        binding.pb.isVisible = it
                    }
                }
                launch {
                    signupViewmodel.isLoading.collect {
                        binding.pb.isVisible = it
                    }
                }
                launch {
                    loginViewModel.loginError.collect { error ->
                        error?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    signupViewmodel.signupError.collect { error ->
                        error?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun isEmailValid() = binding.etUsername.text.toString().isValidEmail()

    private fun isPasswordValid() = binding.etPassword.text.toString().length >= 6


    private fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
