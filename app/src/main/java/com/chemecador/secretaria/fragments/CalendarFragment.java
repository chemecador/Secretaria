package com.chemecador.secretaria.fragments;

import static com.chemecador.secretaria.utils.Utils.ERROR;
import static com.chemecador.secretaria.utils.Utils.showToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.adapters.TaskAdapter;
import com.chemecador.secretaria.api.Client;
import com.chemecador.secretaria.api.Service;
import com.chemecador.secretaria.databinding.FragmentCalendarBinding;
import com.chemecador.secretaria.db.DB;
import com.chemecador.secretaria.gui.CustomToast;
import com.chemecador.secretaria.items.Task;
import com.chemecador.secretaria.logger.Logger;
import com.chemecador.secretaria.requests.TaskRequest;
import com.chemecador.secretaria.responses.IdResponse;
import com.chemecador.secretaria.ui.login.LoginActivity;
import com.chemecador.secretaria.utils.PreferencesHandler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CalendarFragment extends Fragment {

    private final static String TAG = CalendarFragment.class.getSimpleName();
    private FragmentCalendarBinding binding;
    private Button btnDay;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private Context ctx;
    private LocalDateTime selectedDay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);


        init();

        return binding.getRoot();

    }

    private void init() {

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        btnDay = toolbar.findViewById(R.id.btn_day);
        btnDay.setVisibility(View.VISIBLE);
        btnDay.setOnClickListener(v1 -> changeDay());

        taskList = DB.getInstance(ctx).getTasksByDay(LocalDateTime.now());
        taskAdapter = new TaskAdapter(ctx, taskList);


        RecyclerView rv = binding.getRoot().findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(ctx));
        rv.setAdapter(taskAdapter);

        // Obtener una referencia al ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        setDay(year, month + 1, day);

        binding.fab.setOnClickListener(view -> createTask());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void changeDay() {
        DatePicker datePicker = new DatePicker(requireContext());
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), this::onDayChanged, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        dialog.show();
    }

    private void setDay(int year, int monthOfYear, int dayOfMonth) {

        String day = String.format(Locale.getDefault(), "%02d", dayOfMonth);
        String month = String.format(Locale.getDefault(), "%02d", monthOfYear);
        String newDay = (Calendar.getInstance().get(Calendar.YEAR) == year) ? day + "/" + month : day + "/" + month + "/" + year;
        selectedDay = LocalDateTime.of(year, monthOfYear, dayOfMonth, 0, 0, 0);
        btnDay.setText(newDay);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void onDayChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setDay(year, monthOfYear + 1, dayOfMonth);
        taskList.clear();
        taskList.addAll(DB.getInstance(ctx).getTasksByDay(selectedDay));
        taskAdapter.notifyDataSetChanged();
    }


    public void createTask() {

        Task mTask = new Task();
        mTask.setStartTime(selectedDay);

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View dialogView = inflater.inflate(R.layout.dialog_new_task, null);

        EditText editText = dialogView.findViewById(R.id.et_title);
        RadioButton selectTime = dialogView.findViewById(R.id.radio_select_time);
        RadioButton allDayLong = dialogView.findViewById(R.id.radio_all_day_long);

        MaterialCheckBox cbContent = dialogView.findViewById(R.id.cb_content);
        EditText etContent = dialogView.findViewById(R.id.et_content);

        Button positiveButton = dialogView.findViewById(R.id.btn_ok);
        Button negativeButton = dialogView.findViewById(R.id.btn_cancel);


        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        cbContent.addOnCheckedStateChangedListener((checkBox, state) -> {
            if (state == 1) etContent.setVisibility(View.VISIBLE);
            else etContent.setVisibility(View.GONE);
        });


        selectTime.setOnClickListener(v -> {
            selectTime.setChecked(true);
            allDayLong.setChecked(false);
        });

        allDayLong.setOnClickListener(v -> {
            selectTime.setChecked(false);
            allDayLong.setChecked(true);
        });

        positiveButton.setOnClickListener(v -> {
            mTask.setTitle(editText.getText().toString());

            if (etContent.getVisibility() == View.VISIBLE) {
                mTask.setContent(etContent.getText().toString());
            } else {
                mTask.setContent("");
            }
            if (allDayLong.isChecked()) {
                if (PreferencesHandler.isOnline(ctx)) {

                    // Crear un objeto LocalDateTime con la fecha actual y la hora seleccionada
                    LocalDateTime selectedDateTime = LocalDateTime.of(selectedDay.getYear(),
                            selectedDay.getMonth(), selectedDay.getDayOfMonth(), 0, 0, 0);

                    // Asignar el valor formateado al objeto mTask
                    mTask.setStartTime(selectedDateTime);
                    syncTask(mTask);
                } else {
                    insertTask(mTask);
                }
            } else if (selectTime.isChecked()) {
                askTime(mTask);
            }
            dialog.dismiss();
        });
        negativeButton.setOnClickListener(v -> dialog.dismiss());


        dialog.show();

    }

    private void askTime(Task mTask) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                ctx,
                (view, hourOfDay1, minute1) -> {

                    // Crear un objeto LocalDateTime con la fecha actual y la hora seleccionada
                    LocalDateTime selectedDateTime = LocalDateTime.of(selectedDay.getYear(),
                            selectedDay.getMonth(), selectedDay.getDayOfMonth(), hourOfDay1, minute1);



                    // Asignar el valor formateado al objeto mTask
                    mTask.setStartTime(selectedDateTime);

                    if (PreferencesHandler.isOnline(ctx)) {
                        syncTask(mTask);
                    } else {
                        insertTask(mTask);
                    }
                },
                hourOfDay,
                minute,
                true // true para formato de 24 horas, false para formato de 12 horas
        );
        timePickerDialog.show();
    }

    private void insertTask(Task mTask) {
        // Agregar la tarea a la lista
        taskList.add(mTask);

        // Notificar al adaptador del cambio en la lista de tareas
        taskAdapter.notifyItemInserted(taskList.size() - 1);


        // Almacenar la cadena en la base de datos SQLite
        DB.getInstance(ctx).insertTask(mTask);

        Snackbar.make(binding.getRoot(), getString(R.string.create_task_success), Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .show();


        Logger.i(TAG, "Tarea creada correctamente: " + mTask);
    }

    private void syncTask(Task mTask) {

        // Obtener la instancia de Retrofit
        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        int userId = PreferenceManager.getDefaultSharedPreferences(ctx).getInt("id", -1);
        String token = PreferenceManager.getDefaultSharedPreferences(ctx).getString("token", "");

        if (userId == -1) {
            new CustomToast(ctx, ERROR, Toast.LENGTH_LONG).show(getString(R.string.login_again));
            ((Activity) ctx).finish();
            startActivity(new Intent(ctx, LoginActivity.class));
            return;
        }

        TaskRequest tr = new TaskRequest(mTask);

        // Utilizar el servicio para realizar llamadas a la API
        Call<IdResponse> call = apiService.createTask(token, userId, tr);

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<IdResponse>() {
            @Override
            public void onResponse(@NonNull Call<IdResponse> call, @NonNull Response<IdResponse> response) {
                if (response.isSuccessful()) {

                    // Procesar la respuesta exitosa
                    IdResponse result = response.body();
                    if (result == null) return;
                    int id = result.getId();
                    mTask.setId(id);
                    insertTask(mTask);

                } else if (response.code() == 401) {
                    // Manejar el error de respuesta
                    showToast(ctx, ERROR, response.code() + " : " + getString(R.string.unauthorized));
                } else {
                    showToast(ctx, ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<IdResponse> call, @NonNull Throwable t) {

                // Manejar el error de conexión o la excepción
                showToast(ctx, ERROR, getString(R.string.server_error));

            }
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.ctx = context;
    }
}