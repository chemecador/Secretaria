package com.chemecador.secretaria.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.activities.LoginActivity
import com.chemecador.secretaria.databinding.FragmentChangePassBinding
import com.chemecador.secretaria.network.retrofit.Client.client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.requests.PasswordRequest
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePassFragment : Fragment() {
    private var binding: FragmentChangePassBinding? = null
    private var tilOldPass: TextInputLayout? = null
    private var tilNewPass: TextInputLayout? = null
    private var tilConfirmPass: TextInputLayout? = null
    private var etOldPass: EditText? = null
    private var etNewPass: EditText? = null
    private var etConfirmPass: EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePassBinding.inflate(
            layoutInflater
        )
        tilOldPass = binding!!.tilOldPassword
        etOldPass = binding!!.etOldPassword
        tilNewPass = binding!!.tilNewPassword
        etNewPass = binding!!.etNewPassword
        tilConfirmPass = binding!!.tilConfirmPassword
        etConfirmPass = binding!!.etConfirmPassword
        val btnConfirm = binding!!.btnConfirm
        btnConfirm.setOnClickListener { changePassword() }
        return binding!!.root
    }

    private fun changePassword() {
        val oldPasswordStr = etOldPass!!.text.toString()
        val newPasswordStr = etNewPass!!.text.toString()
        val confirmPasswordStr = etConfirmPass!!.text.toString()
        if (oldPasswordStr.isEmpty()) {
            tilNewPass!!.error = getString(R.string.insert_password)
            return
        }
        if (newPasswordStr.isEmpty()) {
            tilNewPass!!.error = getString(R.string.insert_password)
            return
        }
        if (newPasswordStr != confirmPasswordStr) {
            tilConfirmPass!!.error = getString(R.string.unmatched_passwords)
            return
        }

        // Eliminar los mensajes de error
        tilOldPass!!.error = null
        tilNewPass!!.error = null
        tilConfirmPass!!.error = null
        syncPassword(oldPasswordStr, newPasswordStr)
    }

    private fun syncPassword(oldPass: String, newPass: String) {


        // Obtener la instancia de Retrofit
        val retrofit = client

        // Crear una instancia del servicio de la API
        val apiService = retrofit.create(
            Service::class.java
        )
        val userId =
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("id", -1)
        val token =
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("token", "")!!
        if (userId == -1) {
            Toast.makeText(requireContext(),getString(R.string.login_again),Toast.LENGTH_LONG).show()
            (requireContext() as Activity).finish()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return
        }
        val pr = PasswordRequest(oldPass, newPass)

        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService.changePassword(token, userId, pr)

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody: String? =
                        response.body()?.string()
                    if (responseBody == "OK") {
                        Snackbar.make(
                            binding!!.root,
                            R.string.change_password_success,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else if (response.code() == 401) {
                    // Manejar el error de respuesta
                    Utils.showToast(
                        requireContext(),
                        response.code().toString() + " : " + getString(R.string.incorrect_password)
                    )
                } else {
                    Utils.showToast(
                        requireContext(),
                        response.code().toString() + " : " + getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                // Manejar el error de conexión o la excepción
                Utils.showToast(requireContext(), getString(R.string.server_error))
            }
        })
    }
}