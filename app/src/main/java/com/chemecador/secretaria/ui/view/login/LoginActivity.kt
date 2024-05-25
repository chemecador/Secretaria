package com.chemecador.secretaria.ui.view.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Null binding")
    private val loginViewModel: LoginViewModel by viewModels()
    private val signupViewmodel: SignupViewmodel by viewModels()

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
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
        binding.btnSignup.setOnClickListener {
            handleSingup()
        }
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
