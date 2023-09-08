package com.chemecador.secretaria.ui.login;

import static com.chemecador.secretaria.utils.Utils.ERROR;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.activities.MainActivity;
import com.chemecador.secretaria.api.Client;
import com.chemecador.secretaria.api.Service;
import com.chemecador.secretaria.databinding.ActivityLoginBinding;
import com.chemecador.secretaria.db.DB;
import com.chemecador.secretaria.items.Note;
import com.chemecador.secretaria.items.NotesList;
import com.chemecador.secretaria.items.Task;
import com.chemecador.secretaria.logger.Logger;
import com.chemecador.secretaria.requests.login.LoginRequest;
import com.chemecador.secretaria.responses.login.LoginResponse;
import com.chemecador.secretaria.utils.PreferencesHandler;
import com.chemecador.secretaria.utils.Utils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = LoginActivity.this.getClass().getSimpleName();
    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private String username;
    private String password;
    private CountDownTimer progressTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Logger.crearSingleton(this);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);


        if (PreferencesHandler.isTokenValid(this)) {
            syncDB();
            return;
        } else {
            binding.loading.setVisibility(View.GONE);
        }


        final EditText usernameEditText = binding.etUsername;
        final EditText passwordEditText = binding.etPassword;
        final Button loginButton = binding.btnLogin;
        final Button guestButton = binding.btnGuest;
        final Button registerbutton = binding.btnRegister;


        guestButton.setOnClickListener(v -> loginOffline());

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            registerbutton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser();
            }
            binding.tilUser.setHelperText("");
            setResult(Activity.RESULT_OK);


            // Obtener la instancia de Retrofit
            Retrofit retrofit = Client.getClient();

            // Crear una instancia del servicio de la API
            Service apiService = retrofit.create(Service.class);

            LoginRequest request = new LoginRequest(username, password);

            // Utilizar el servicio para realizar llamadas a la API
            Call<LoginResponse> call = apiService.login(request);

            // Ejecutar la llamada de forma asíncrona
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                    if (response.isSuccessful()) {
                        LoginResponse result = response.body();
                        assert result != null;
                        PreferencesHandler.save(LoginActivity.this, result.getId(), "Bearer " + result.getToken());
                        syncDB();
                    } else {
                        binding.loading.setVisibility(View.GONE);
                        enableButtons();
                        Logger.e(TAG, "Error en el login" + response.code() + " - " + response.message());
                        if (response.code() == 401) {
                            Utils.showToast(LoginActivity.this, ERROR, getString(R.string.login_incorrect));
                        } else if (response.code() == 403) {
                            Utils.showToast(LoginActivity.this, ERROR, getString(R.string.user_already_exists));
                        } else {
                            Utils.showToast(LoginActivity.this, ERROR, getString(R.string.server_error));
                        }
                    }

                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {

                    binding.loading.setVisibility(View.GONE);
                    enableButtons();

                    Logger.e(TAG, "Error en el login ", t);
                    Utils.showToast(LoginActivity.this, ERROR, getString(R.string.connection_error));
                }
            });
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {

            final TextInputLayout tilPassword = binding.getRoot().findViewById(R.id.til_password);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
                if (loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString())) {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                    binding.tilUser.setHelperText(getString(R.string.only_use));
                } else {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    binding.tilUser.setHelperText(getString(R.string.never_spam));
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {

                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                if (loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString())) {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                } else {
                    tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE);
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> login());

        registerbutton.setOnClickListener(v -> register());
    }

    private void login() {
        disableButtons();
        binding.loading.setVisibility(View.VISIBLE); // Mostrar el AlertDialog
        loginViewModel.login(binding.etUsername.getText().toString(),
                binding.etPassword.getText().toString());
    }

    private void register() {
        username = binding.etUsername.getText().toString();
        password = binding.etPassword.getText().toString();
        disableButtons();
        binding.loading.setVisibility(View.VISIBLE);

        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        LoginRequest request = new LoginRequest(username, password);

        // Utilizar el servicio para realizar llamadas a la API
        Call<LoginResponse> call = apiService.register(request);

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                enableButtons();

                if (response.isSuccessful()) {

                    LoginResponse result = response.body();
                    assert result != null;
                    PreferencesHandler.save(LoginActivity.this, result.getId(), "Bearer " + result.getToken());
                    finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                } else if (response.code() == 403){
                    binding.loading.setVisibility(View.GONE);
                    Logger.e(TAG, "Error en el login" + response.code() + " - " + response.message());
                    Utils.showToast(LoginActivity.this, ERROR, getString(R.string.user_already_exists));
                }else {
                    binding.loading.setVisibility(View.GONE);
                    Logger.e(TAG, "Error en el login" + response.code() + " - " + response.message());
                    Utils.showToast(LoginActivity.this, ERROR, getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                binding.loading.setVisibility(View.GONE);
                enableButtons();

                Logger.e(TAG, "Error en el login: ", t);
                Utils.showToast(LoginActivity.this, ERROR, getString(R.string.connection_error));
            }
        });
    }

    private void loginOffline() {
        PreferencesHandler.clear(this);
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private void syncDB() {

        syncLists();

        startProgressTimer(); // Iniciar el temporizador
    }

    private void startProgressTimer() {
        progressTimer = new CountDownTimer(10000, 1000) { // 10 segundos de tiempo de espera
            public void onTick(long millisUntilFinished) {
                // No se requiere ninguna acción en cada tick del temporizador
            }

            public void onFinish() {
                binding.loading.setVisibility(View.GONE);
                enableButtons();
            }
        }.start();
    }

    private void syncLists() {

        // Obtener la instancia de Retrofit
        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        // Utilizar el servicio para realizar llamadas a la API
        Call<ArrayList<NotesList>> call = apiService.getLists(PreferencesHandler.getToken(this), PreferencesHandler.getId(this));

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<ArrayList<NotesList>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<NotesList>> call, @NonNull Response<ArrayList<NotesList>> response) {
                if (response.isSuccessful()) {

                    // Procesar la respuesta exitosa
                    ArrayList<NotesList> result = response.body();
                    assert result != null;
                    if (DB.getInstance(LoginActivity.this).setLists(result)) {
                        syncNotes();
                    } else {
                        Utils.showToast(LoginActivity.this, ERROR, R.string.something_went_wrong);
                    }
                } else if (response.code() == 401) {
                    PreferencesHandler.clear(LoginActivity.this);
                    binding.loading.setVisibility(View.GONE);
                    enableButtons();
                    // Manejar el error de respuesta
                    Utils.showToast(LoginActivity.this, ERROR, response.code() + " : " + getString(R.string.unauthorized));
                } else {
                    binding.loading.setVisibility(View.GONE);
                    enableButtons();
                    Utils.showToast(LoginActivity.this, ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<NotesList>> call, @NonNull Throwable t) {
                binding.loading.setVisibility(View.GONE);
                enableButtons();
                // Manejar el error de conexión o la excepción
                Utils.showToast(LoginActivity.this, ERROR, getString(R.string.connection_error));
            }
        });
    }

    private void syncNotes() {

        // Obtener la instancia de Retrofit
        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        // Utilizar el servicio para realizar llamadas a la API
        Call<ArrayList<Note>> call = apiService.getNotes(
                PreferencesHandler.getToken(this), PreferencesHandler.getId(this));

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<ArrayList<Note>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Note>> call, @NonNull Response<ArrayList<Note>> response) {
                if (response.isSuccessful()) {

                    // Procesar la respuesta exitosa
                    ArrayList<Note> result = response.body();
                    if (result != null) {
                        DB.getInstance(LoginActivity.this).setNotes(result);
                    }
                    syncTasks();

                } else if (response.code() == 401) {
                    binding.loading.setVisibility(View.GONE);
                    enableButtons();
                    // Manejar el error de respuesta
                    Utils.showToast(LoginActivity.this, ERROR, response.code() + " : " + getString(R.string.unauthorized));
                } else {
                    binding.loading.setVisibility(View.GONE);
                    enableButtons();
                    Utils.showToast(LoginActivity.this, ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Note>> call, @NonNull Throwable t) {

                binding.loading.setVisibility(View.GONE);
                enableButtons();
                syncTasks();
                // Manejar el error de conexión o la excepción
                Utils.showToast(LoginActivity.this, ERROR, getString(R.string.connection_error));
            }
        });
    }

    private void syncTasks() {

        // Obtener la instancia de Retrofit
        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        // Utilizar el servicio para realizar llamadas a la API
        Call<ArrayList<Task>> call = apiService.getTasks(
                PreferencesHandler.getToken(this), PreferencesHandler.getId(this));

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<ArrayList<Task>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Task>> call, @NonNull Response<ArrayList<Task>> response) {
                if (response.isSuccessful()) {

                    // Procesar la respuesta exitosa
                    ArrayList<Task> result = response.body();
                    assert result != null;
                    DB.getInstance(LoginActivity.this).setTasks(result);

                    onSyncFinished();

                } else if (response.code() == 401) {
                    binding.loading.setVisibility(View.GONE);
                    enableButtons();
                    // Manejar el error de respuesta
                    Utils.showToast(LoginActivity.this, ERROR, response.code() + " : " + getString(R.string.unauthorized));
                } else {
                    binding.loading.setVisibility(View.GONE);
                    enableButtons();
                    Utils.showToast(LoginActivity.this, ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Task>> call, @NonNull Throwable t) {
                onSyncFinished();
                // Manejar el error de conexión o la excepción
                Utils.showToast(LoginActivity.this, ERROR, getString(R.string.connection_error));
            }
        });
    }

    public void onSyncFinished() {
        if (progressTimer != null) {
            progressTimer.cancel(); // Cancelar el temporizador
        }
        binding.loading.setVisibility(View.GONE);
        enableButtons();// Ocultar el AlertDialog
        finish();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    private void updateUiWithUser() {
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void disableButtons() {
        binding.tilUser.setHelperText("");
        binding.btnLogin.setEnabled(false);
        binding.btnRegister.setEnabled(false);
        binding.btnGuest.setEnabled(false);
    }

    private void enableButtons() {
        binding.btnLogin.setEnabled(true);
        binding.btnRegister.setEnabled(true);
        binding.btnGuest.setEnabled(true);
    }
}