package com.chemecador.secretaria.adapters;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.api.Client;
import com.chemecador.secretaria.api.Service;
import com.chemecador.secretaria.db.DB;
import com.chemecador.secretaria.items.Task;
import com.chemecador.secretaria.logger.Logger;
import com.chemecador.secretaria.requests.TaskRequest;
import com.chemecador.secretaria.utils.PreferencesHandler;
import com.chemecador.secretaria.utils.Utils;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private final static String TAG = TaskAdapter.class.getSimpleName();
    private final List<Task> taskList;
    private final LayoutInflater mInflater;
    private final Context ctx;
    private AlertDialog dialog;

    public TaskAdapter(Context ctx, List<Task> tasks) {
        this.ctx = ctx;
        this.mInflater = LayoutInflater.from(ctx);
        this.taskList = tasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.item_task, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener la tarea actual
        Task mTask = taskList.get(position);
        holder.bindData(mTask);
        // Asignar un listener de clic al elemento de la lista
        holder.itemView.setOnClickListener(v -> showTask(mTask));
    }

    private void showTask(Task mTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View dialogView = inflater.inflate(R.layout.detail_task, null);

        builder.setView(dialogView);

        TextInputLayout tilTitle = dialogView.findViewById(R.id.til_title);
        Objects.requireNonNull(tilTitle.getEditText()).setText(mTask.getTitle());
        EditText etContent = dialogView.findViewById(R.id.et_content);
        etContent.setText(mTask.getContent());

        String sTime = mTask.getStartTime().format(Utils.getFullFormatter());
        TextView tvTime = dialogView.findViewById(R.id.tv_start_time);
        tvTime.setText(Utils.beautifyDate(sTime));
        tvTime.setOnClickListener(v12 -> {
            // Mostrar el selector de fecha
            showDatePicker(mTask);
        });

        Button btnUpdate = dialogView.findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(v1 -> {

            String newTitle = tilTitle.getEditText().getText().toString();
            String newContent = etContent.getText().toString();

            if (newTitle.isEmpty()) {
                tilTitle.setError(ctx.getString(R.string.error_empty_field));
                return;
            }
            mTask.setTitle(newTitle);
            mTask.setContent(newContent);
            updateTaskOnline(mTask);
        });
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v1 -> deleteTask(mTask));

        dialog = builder.show();
    }

    private void showDatePicker(Task mTask) {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear un DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ctx,
                (view, year, month, dayOfMonth) -> {
                    // Aquí se ejecutará cuando el usuario seleccione la nueva fecha
                    // year, month y dayOfMonth contienen la nueva fecha seleccionada

                    // Crear un objeto LocalDateTime con la nueva fecha y la hora existente
                    LocalDateTime newDateTime = mTask.getStartTime()
                            .withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);

                    // Actualizar la fecha y hora en la tarea actual
                    mTask.setStartTime(newDateTime);

                    // Actualizar la vista en el diálogo de detalle
                    TextView tvTime = dialog.findViewById(R.id.tv_start_time);
                    tvTime.setText(Utils.beautifyDate(newDateTime.format(Utils.getFullFormatter())));
                },
                currentYear,
                currentMonth,
                currentDay
        );
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) ->

                // Mostrar el selector de tiempo después de seleccionar la fecha
                showTimePicker(mTask, year, month + 1, dayOfMonth));

        // Mostrar el diálogo del selector de fecha
        datePickerDialog.show();
    }

    private void showTimePicker(Task mTask, int year, int month, int day) {
        // Obtener la hora y minuto actuales
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Crear un TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                ctx,
                (view, hourOfDay, minute) -> {
                    // Aquí se ejecutará cuando el usuario seleccione la nueva hora
                    // hourOfDay y minute contienen la nueva hora seleccionada

                    // Crear un objeto LocalDateTime con la fecha existente y la nueva hora y minutos
                    LocalDateTime newTime = LocalDateTime.of(year, month, day, hourOfDay, minute);

                    // Actualizar el tiempo en la tarea actual
                    mTask.setStartTime(newTime);

                    // Actualizar la vista en el diálogo de detalle
                    TextView tvTime = dialog.findViewById(R.id.tv_start_time);
                    tvTime.setText(Utils.beautifyDate(newTime.format(Utils.getFullFormatter())));
                },
                currentHour,
                currentMinute,
                false
        );

        // Mostrar el diálogo del selector de tiempo
        timePickerDialog.show();
    }

    private void updateTaskOnline(Task mTask) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            Retrofit retrofit = Client.getClient();

            // Crear una instancia del servicio de la API
            Service apiService = retrofit.create(Service.class);

            TaskRequest tr = new TaskRequest(mTask);

            // Utilizar el servicio para realizar llamadas a la API
            Call<ResponseBody> call = apiService.updateTask(
                    PreferencesHandler.getToken(ctx), PreferencesHandler.getId(ctx), mTask.getId(), tr);

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
                            updateTaskFromDB(mTask);
                        } else {
                            Utils.showToast(ctx, Utils.SUCCESS,
                                    ctx.getString(R.string.update_error) + ": " + responseBody);
                        }
                    } else {
                        Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.update_error));
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.connection_error));
                }
            });
        } else {
            updateTaskFromDB(mTask);
        }
    }

    private void updateTaskFromDB(Task mTask) {
        int updatedTasks = DB.getInstance(ctx).updateTask(mTask);
        if (updatedTasks == 0) {
            Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.updated_zero));
        } else {
            Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.update_success));
            Logger.i(TAG, "Tarea actualizada correctamente: " + mTask);
        }


        //noinspection NotifyDataSetChanged
        notifyDataSetChanged();
        if (dialog.isShowing()) dialog.dismiss();

    }

    private void deleteTask(Task mTask) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            Retrofit retrofit = Client.getClient();

            // Crear una instancia del servicio de la API
            Service apiService = retrofit.create(Service.class);

            // Utilizar el servicio para realizar llamadas a la API
            Call<ResponseBody> call = apiService.deleteTask(
                    PreferencesHandler.getToken(ctx), PreferencesHandler.getId(ctx), mTask.getId());

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
                            deleteTaskFromDB(mTask);
                        } else {
                            Utils.showToast(ctx, Utils.SUCCESS,
                                    ctx.getString(R.string.delete_error) + ": " + responseBody);
                        }
                    } else {
                        Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.delete_error));
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.connection_error));
                }
            });
        } else {
            deleteTaskFromDB(mTask);
        }
    }

    private void deleteTaskFromDB(Task mTask) {
        int deletedTasks = DB.getInstance(ctx).delete(DB.TASKS_TABLE, mTask.getId());
        if (deletedTasks == 0) {
            Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.delete_zero));
        } else {
            // La nota se eliminó correctamente
            Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.delete_success));
            Logger.i(TAG, "Tarea eliminada correctamente: " + mTask);
            taskList.remove(mTask);
            //noinspection NotifyDataSetChanged
            notifyDataSetChanged();
        }

        if (dialog.isShowing()) dialog.dismiss();
    }

    @Override
    public int getItemCount() {

        if (taskList == null) return 0;

        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        public void bindData(Task task) {
            tvTitle.setText(task.getTitle());
        }
    }
}
