package com.chemecador.secretaria.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.chemecador.secretaria.R
import com.chemecador.secretaria.api.Client.client
import com.chemecador.secretaria.api.Service
import com.chemecador.secretaria.databinding.ActivityLoginBinding
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.logger.Logger.Companion.e
import com.chemecador.secretaria.requests.LoginRequest
import com.chemecador.secretaria.responses.login.LoginResponse
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {
    private val className = this@LoginActivity.javaClass.simpleName
    var binding: ActivityLoginBinding? = null
    private var username: String? = null
    private var password: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        Logger.crearSingleton(this)
        if (PreferencesHandler.isTokenValid(this)) {
            syncDB()
            return
        } else {
            binding!!.loading.visibility = View.GONE
        }
        val usernameEditText: EditText = binding!!.etUsername
        val passwordEditText: EditText = binding!!.etPassword
        val loginButton = binding!!.btnLogin
        val guestButton = binding!!.btnGuest
        val registerbutton = binding!!.btnRegister

        enableButtons()
        guestButton.setOnClickListener { loginOffline() }

        /*val afterTextChangedListener: TextWatcher = object : TextWatcher {
            val tilPassword: TextInputLayout =
                binding!!.root.findViewById<TextInputLayout>(R.id.til_password)

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
                if (loginViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                    )
                ) {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE)
                    binding!!.tilUser.helperText = getString(R.string.only_use)
                } else {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE)
                    binding!!.tilUser.helperText = getString(R.string.never_spam)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                username = usernameEditText.getText().toString()
                password = passwordEditText.getText().toString()
                if (loginViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                    )
                ) {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE)
                } else {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE)
                }
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.getText().toString(),
                    passwordEditText.getText().toString()
                )
            }
            false
        })*/
        loginButton.setOnClickListener { login() }
        registerbutton.setOnClickListener { register() }
    }

    private fun login() {
        disableButtons()
        binding!!.loading.visibility = View.VISIBLE // Mostrar el AlertDialog

        username = binding?.etUsername?.text.toString()
        password = binding?.etPassword?.text.toString()
        // Obtener la instancia de Retrofit
        val retrofit = client

        // Crear una instancia del servicio de la API
        val apiService = retrofit!!.create(
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
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    e(className, "Error en el login" + response.code() + " - " + response.message())
                    if (response.code() == 401) {
                        Utils.showToast(
                            this@LoginActivity,
                            Utils.ERROR,
                            getString(R.string.login_incorrect)
                        )
                    } else if (response.code() == 403) {
                        Utils.showToast(
                            this@LoginActivity,
                            Utils.ERROR,
                            getString(R.string.user_already_exists)
                        )
                    } else {
                        Utils.showToast(
                            this@LoginActivity,
                            Utils.ERROR,
                            getString(R.string.server_error)
                        )
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                binding!!.loading.visibility = View.GONE
                enableButtons()
                e(className, "Error en el login ", t)
                Utils.showToast(
                    this@LoginActivity,
                    Utils.ERROR,
                    getString(R.string.connection_error)
                )
            }
        })
    }


    private fun register() {
        val username = binding!!.etUsername.text.toString()
        val password = binding!!.etPassword.text.toString()
        disableButtons()
        binding!!.loading.visibility = View.VISIBLE
        val retrofit: Retrofit? = client

        // Crear una instancia del servicio de la API
        val apiService: Service? = retrofit?.create(
            Service::class.java
        )
        val request = LoginRequest(username, password)

        // Utilizar el servicio para realizar llamadas a la API
        val call: Call<LoginResponse?>? = apiService?.register(request)

        // Ejecutar la llamada de forma asíncrona
        call!!.enqueue(object : Callback<LoginResponse?> {
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
                    binding!!.loading.visibility = View.GONE
                    e(
                        className,
                        "Error en el login" + response.code() + " - " + response.message()
                    )
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        getString(R.string.user_already_exists)
                    )
                } else {
                    binding!!.loading.visibility = View.GONE
                    e(
                        className,
                        "Error en el login" + response.code() + " - " + response.message()
                    )
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                binding!!.loading.visibility = View.GONE
                enableButtons()
                e(className, "Error en el login: ", t)
                Utils.showToast(
                    this@LoginActivity,
                    Utils.ERROR,
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
        syncLists()
    }

    private fun syncLists() {

        // Obtener la instancia de Retrofit
        val retrofit: Retrofit = client!!

        // Crear una instancia del servicio de la API
        val apiService: Service = retrofit.create(
            Service::class.java
        )

        // Utilizar el servicio para realizar llamadas a la API
        val call: Call<ArrayList<NotesList>> =
            apiService.getLists(PreferencesHandler.getToken(this), PreferencesHandler.getId(this))

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<ArrayList<NotesList>> {
            override fun onResponse(
                call: Call<ArrayList<NotesList>>,
                response: Response<ArrayList<NotesList>>
            ) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result: ArrayList<NotesList> = response.body()!!
                    if (DB.getInstance(this@LoginActivity)
                            .setLists(result)
                    ) {
                        syncNotes()
                    } else {
                        Utils.showToast(
                            this@LoginActivity,
                            Utils.ERROR,
                            R.string.something_went_wrong
                        )
                    }
                } else if (response.code() == 401) {
                    PreferencesHandler.clear(this@LoginActivity)
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    // Manejar el error de respuesta
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.unauthorized)
                    )
                } else {
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<ArrayList<NotesList>>, t: Throwable) {
                binding!!.loading.visibility = View.GONE
                enableButtons()
                // Manejar el error de conexión o la excepción
                Utils.showToast(
                    this@LoginActivity,
                    Utils.ERROR,
                    getString(R.string.connection_error)
                )
            }
        })
    }

    private fun syncNotes() {

        // Obtener la instancia de Retrofit
        val retrofit: Retrofit? = client

        // Crear una instancia del servicio de la API
        val apiService: Service? = retrofit?.create(
            Service::class.java
        )

        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService?.getNotes(
            PreferencesHandler.getToken(this), PreferencesHandler.getId(this)
        )

        // Ejecutar la llamada de forma asíncrona
        call!!.enqueue(object : Callback<ArrayList<Note>> {
            override fun onResponse(
                call: Call<ArrayList<Note>>,
                response: Response<ArrayList<Note>>
            ) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result = response.body()
                    if (result != null) {
                        DB.getInstance(this@LoginActivity).setNotes(result)
                    }
                    syncTasks()
                } else if (response.code() == 401) {
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    // Manejar el error de respuesta
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.unauthorized)
                    )
                } else {
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<ArrayList<Note>>, t: Throwable) {
                binding!!.loading.visibility = View.GONE
                enableButtons()
                syncTasks()
                // Manejar el error de conexión o la excepción
                Utils.showToast(
                    this@LoginActivity,
                    Utils.ERROR,
                    getString(R.string.connection_error)
                )
            }
        })
    }

    private fun syncTasks() {

        // Obtener la instancia de Retrofit
        val retrofit: Retrofit? = client

        // Crear una instancia del servicio de la API
        val apiService: Service? = retrofit?.create(
            Service::class.java
        )

        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService?.getTasks(
            PreferencesHandler.getToken(this), PreferencesHandler.getId(this)
        )

        // Ejecutar la llamada de forma asíncrona
        call!!.enqueue(object : Callback<ArrayList<Task>> {
            override fun onResponse(
                call: Call<ArrayList<Task>>,
                response: Response<ArrayList<Task>>
            ) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result = response.body()!!
                    DB.getInstance(this@LoginActivity)
                        .setTasks(result)
                    onSyncFinished()
                } else if (response.code() == 401) {
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    // Manejar el error de respuesta
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.unauthorized)
                    )
                } else {
                    binding!!.loading.visibility = View.GONE
                    enableButtons()
                    Utils.showToast(
                        this@LoginActivity,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<ArrayList<Task>>, t: Throwable) {
                onSyncFinished()
                // Manejar el error de conexión o la excepción
                Utils.showToast(
                    this@LoginActivity,
                    Utils.ERROR,
                    getString(R.string.connection_error)
                )
            }
        })
    }

    fun onSyncFinished() {
        binding!!.loading.visibility = View.GONE
        enableButtons() // Ocultar el AlertDialog
        finish()
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }

    private fun updateUiWithUser() {}
    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun disableButtons() {
        binding!!.tilUser.helperText = ""
        binding!!.btnLogin.isEnabled = false
        binding!!.btnRegister.isEnabled = false
        binding!!.btnGuest.isEnabled = false
    }

    private fun enableButtons() {
        binding!!.btnLogin.isEnabled = true
        binding!!.btnRegister.isEnabled = true
        binding!!.btnGuest.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        enableButtons()
    }
}
