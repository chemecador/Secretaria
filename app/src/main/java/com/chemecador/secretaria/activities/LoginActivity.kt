package com.chemecador.secretaria.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.ActivityLoginBinding
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.logger.Logger.Companion.e
import com.chemecador.secretaria.network.retrofit.Client.client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.network.sync.SyncLists
import com.chemecador.secretaria.network.sync.SyncNotes
import com.chemecador.secretaria.network.sync.SyncTasks
import com.chemecador.secretaria.requests.LoginRequest
import com.chemecador.secretaria.responses.login.LoginResponse
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.chemecador.secretaria.utils.Version
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {
    private val className = this@LoginActivity.javaClass.simpleName
    private lateinit var binding: ActivityLoginBinding
    private var username: String? = null
    private var password: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Logger.crearSingleton(this)
        if (PreferencesHandler.isTokenValid(this) && PreferencesHandler.lastLoginOk(this)) {
            PreferencesHandler.putBoolean(this, PreferencesHandler.PREF_LAST_LOGIN_OK, false)
            syncDB()
            return
        }
        showWelcome()
        enableButtons()
        binding.btnGuest.setOnClickListener { loginOffline() }
        binding.btnLogin.setOnClickListener { login() }
        binding.btnRegister.setOnClickListener { register() }
    }

    private fun showWelcome() {

        if (PreferencesHandler.getBoolean(this, PreferencesHandler.PREF_NEW_USER, true)) {
            PreferencesHandler.putBoolean(this, PreferencesHandler.PREF_NEW_USER, false)
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.welcome_title)
                .setMessage(R.string.welcome_msg)
                .setCancelable(false)
                .setNeutralButton(R.string.understood) { _, _ -> Version.showPatchNotes(context = this) }
                .show()
        } else if (PreferencesHandler.getBoolean(this, PreferencesHandler.PREF_NEW_VERSION, true)){
            Version.showPatchNotes(context = this)
        }


        PreferencesHandler.putBoolean(this, PreferencesHandler.PREF_NEW_VERSION, false)
    }

    private fun login() {
        disableButtons()
        binding.loading.visibility = View.VISIBLE // Mostrar el AlertDialog

        username = binding.etUsername.text.toString()
        password = binding.etPassword.text.toString()
        // Obtener la instancia de Retrofit
        val retrofit = client

        // Crear una instancia del servicio de la API
        val apiService = retrofit.create(
            Service::class.java
        )
        val request = LoginRequest(username, password)

        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService.login(request)

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<LoginResponse?> {
            override fun onResponse(
                call: Call<LoginResponse?>,
                response: Response<LoginResponse?>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()!!
                    PreferencesHandler.save(
                        this@LoginActivity,
                        result.id,
                        "Bearer " + result.token
                    )
                    syncDB()
                } else {
                    binding.loading.visibility = View.GONE
                    enableButtons()
                    e(className, "Error en el login" + response.code() + " - " + response.message())
                    if (response.code() == 401) {
                        Utils.showToast(
                            this@LoginActivity,
                            getString(R.string.login_incorrect)
                        )
                    } else if (response.code() == 403) {
                        Utils.showToast(
                            this@LoginActivity,
                            getString(R.string.user_already_exists)
                        )
                    } else {
                        Utils.showToast(
                            this@LoginActivity,
                            getString(R.string.server_error)
                        )
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                binding.loading.visibility = View.GONE
                enableButtons()
                e(className, "Error en el login ", t)
                Utils.showToast(
                    this@LoginActivity,
                    getString(R.string.connection_error)
                )
            }
        })
    }

    private fun register() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        disableButtons()
        binding.loading.visibility = View.VISIBLE
        val retrofit: Retrofit = client

        // Crear una instancia del servicio de la API
        val apiService: Service = retrofit.create(
            Service::class.java
        )
        val request = LoginRequest(username, password)

        // Utilizar el servicio para realizar llamadas a la API
        val call: Call<LoginResponse?> = apiService.register(request)

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<LoginResponse?> {
            override fun onResponse(
                call: Call<LoginResponse?>,
                response: Response<LoginResponse?>
            ) {
                enableButtons()
                if (response.isSuccessful) {
                    val result: LoginResponse = response.body()!!
                    PreferencesHandler.save(this@LoginActivity, result.id, "Bearer " + result.token)
                    finish()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else if (response.code() == 403) {
                    binding.loading.visibility = View.GONE
                    e(
                        className,
                        "Error en el login" + response.code() + " - " + response.message()
                    )
                    Utils.showToast(
                        this@LoginActivity,
                        getString(R.string.user_already_exists)
                    )
                } else {
                    binding.loading.visibility = View.GONE
                    e(
                        className,
                        "Error en el login" + response.code() + " - " + response.message()
                    )
                    Utils.showToast(
                        this@LoginActivity,
                        getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                binding.loading.visibility = View.GONE
                enableButtons()
                e(className, "Error en el login: ", t)
                Utils.showToast(
                    this@LoginActivity,
                    getString(R.string.connection_error)
                )
            }
        })
    }

    private fun loginOffline() {
        PreferencesHandler.clear(this)
        finish()
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    private fun syncDB() {
        SyncLists.getLists(this) { listsSuccess ->
            if (listsSuccess) {
                SyncNotes.getNotes(this) { notesSuccess ->
                    if (notesSuccess) {
                        SyncTasks.getTasks(this) { tasksSuccess ->
                            if (tasksSuccess) {
                                onSyncFinished()
                            } else {
                                enableButtons()
                            }
                        }
                    } else {
                        enableButtons()
                    }
                }
            } else {
                enableButtons()
            }
        }
    }

    private fun onSyncFinished() {
        binding.loading.visibility = View.GONE
        enableButtons() // Ocultar el AlertDialog
        finish()
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }

    private fun disableButtons() {
        binding.tilUser.helperText = ""
       // binding.btnLogin.isEnabled = false
        binding.btnRegister.isEnabled = false
        binding.btnGuest.isEnabled = false
    }

    private fun enableButtons() {
        binding.loading.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnRegister.isEnabled = true
        binding.btnGuest.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        enableButtons()
    }
}
