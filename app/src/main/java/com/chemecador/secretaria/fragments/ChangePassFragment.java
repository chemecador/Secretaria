package com.chemecador.secretaria.fragments;

import static com.chemecador.secretaria.utils.Utils.ERROR;
import static com.chemecador.secretaria.utils.Utils.showToast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.api.Client;
import com.chemecador.secretaria.api.Service;
import com.chemecador.secretaria.databinding.FragmentChangePassBinding;
import com.chemecador.secretaria.gui.CustomToast;
import com.chemecador.secretaria.requests.PasswordRequest;
import com.chemecador.secretaria.ui.login.LoginActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChangePassFragment extends Fragment {

    private FragmentChangePassBinding binding;
    private TextInputLayout tilOldPass, tilNewPass, tilConfirmPass;
    private EditText etOldPass, etNewPass, etConfirmPass;

    public ChangePassFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChangePassBinding.inflate(getLayoutInflater());


        tilOldPass = binding.tilOldPassword;
        etOldPass = binding.etOldPassword;

        tilNewPass = binding.tilNewPassword;
        etNewPass = binding.etNewPassword;

        tilConfirmPass = binding.tilConfirmPassword;
        etConfirmPass = binding.etConfirmPassword;

        Button btnConfirm = binding.btnConfirm;

        btnConfirm.setOnClickListener(v -> changePassword());

        return binding.getRoot();
    }

    private void changePassword() {
        String oldPasswordStr = etOldPass.getText().toString();
        String newPasswordStr = etNewPass.getText().toString();
        String confirmPasswordStr = etConfirmPass.getText().toString();

        if (oldPasswordStr.isEmpty()) {
            tilNewPass.setError(getString(R.string.insert_password));
            return;
        }
        if (newPasswordStr.isEmpty()) {
            tilNewPass.setError(getString(R.string.insert_password));
            return;
        }

        if (!newPasswordStr.equals(confirmPasswordStr)) {
            tilConfirmPass.setError(getString(R.string.unmatched_passwords));
            return;
        }

        // Eliminar los mensajes de error
        tilOldPass.setError(null);
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        syncPassword(oldPasswordStr, newPasswordStr);
    }

    private void syncPassword(String oldPass, String newPass) {


        // Obtener la instancia de Retrofit
        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        int userId = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("id", -1);
        String token = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("token", "");

        if (userId == -1) {
            new CustomToast(requireContext(), ERROR, Toast.LENGTH_LONG).show(getString(R.string.login_again));
            ((Activity) requireContext()).finish();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }

        PasswordRequest pr = new PasswordRequest(oldPass, newPass);

        // Utilizar el servicio para realizar llamadas a la API
        Call<ResponseBody> call = apiService.changePassword(token, userId, pr);

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    String responseBody;
                    try {
                        assert response.body() != null;
                        responseBody = response.body().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (responseBody.equals("OK")) {
                        Snackbar.make(binding.getRoot(), R.string.change_password_success, Snackbar.LENGTH_LONG).show();
                    }

                } else if (response.code() == 401) {
                    // Manejar el error de respuesta
                    showToast(requireContext(), ERROR, response.code() + " : " + getString(R.string.incorrect_password));
                } else {
                    showToast(requireContext(), ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                // Manejar el error de conexión o la excepción
                showToast(requireContext(), ERROR, getString(R.string.server_error));

            }
        });
        
        
        
    }
}
