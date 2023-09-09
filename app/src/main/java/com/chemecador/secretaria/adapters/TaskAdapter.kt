package com.chemecador.secretaria.adapters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.api.Client
import com.chemecador.secretaria.api.Service
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.requests.TaskRequest
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.Calendar

class TaskAdapter(private val ctx: Context, private val taskList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(ctx)
    private var dialog: AlertDialog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = mInflater.inflate(R.layout.item_task, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtener la tarea actual
        val mTask = taskList[position]
        holder.bindData(mTask)
        // Asignar un listener de clic al elemento de la lista
        holder.itemView.setOnClickListener { showTask(mTask) }
    }

    private fun showTask(mTask: Task) {
        val builder = AlertDialog.Builder(ctx)
        val inflater = LayoutInflater.from(ctx)
        val dialogView = inflater.inflate(R.layout.detail_task, null)
        builder.setView(dialogView)
        val tilTitle = dialogView.findViewById<TextInputLayout>(R.id.til_title)
        tilTitle.editText!!.setText(mTask.title)
        val etContent = dialogView.findViewById<EditText>(R.id.et_content)
        etContent.setText(mTask.content)
        val sTime = mTask.startTime!!.format(Utils.getFullFormatter())
        val tvTime = dialogView.findViewById<TextView>(R.id.tv_start_time)
        tvTime.text = Utils.beautifyDate(sTime)
        tvTime.setOnClickListener {
            // Mostrar el selector de fecha
            showDatePicker(mTask)
        }
        val btnUpdate = dialogView.findViewById<Button>(R.id.btn_update)
        btnUpdate.setOnClickListener {
            val newTitle = tilTitle.editText!!.text.toString()
            val newContent = etContent.text.toString()
            if (newTitle.isEmpty()) {
                tilTitle.error = ctx.getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            mTask.title = newTitle
            mTask.content = newContent
            updateTaskOnline(mTask)
        }
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener { deleteTask(mTask) }
        dialog = builder.show()
    }

    private fun showDatePicker(mTask: Task) {
        // Obtener la fecha actual
        val calendar = Calendar.getInstance()
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH]
        val currentDay = calendar[Calendar.DAY_OF_MONTH]

        // Crear un DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            ctx,
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                // Aquí se ejecutará cuando el usuario seleccione la nueva fecha
                // year, month y dayOfMonth contienen la nueva fecha seleccionada

                // Crear un objeto LocalDateTime con la nueva fecha y la hora existente
                val newDateTime = mTask.startTime?.withYear(year)?.withMonth(month + 1)?.withDayOfMonth(dayOfMonth)

                // Actualizar la fecha y hora en la tarea actual
                mTask.startTime = newDateTime

                // Actualizar la vista en el diálogo de detalle
                val tvTime = dialog!!.findViewById<TextView>(R.id.tv_start_time)
                tvTime.text = Utils.beautifyDate(newDateTime?.format(Utils.getFullFormatter()))
            },
            currentYear,
            currentMonth,
            currentDay
        )
        datePickerDialog.setOnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->  // Mostrar el selector de tiempo después de seleccionar la fecha
            showTimePicker(mTask, year, month + 1, dayOfMonth)
        }

        // Mostrar el diálogo del selector de fecha
        datePickerDialog.show()
    }

    private fun showTimePicker(mTask: Task, year: Int, month: Int, day: Int) {
        // Obtener la hora y minuto actuales
        val calendar = Calendar.getInstance()
        val currentHour = calendar[Calendar.HOUR_OF_DAY]
        val currentMinute = calendar[Calendar.MINUTE]

        // Crear un TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            ctx,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                // Aquí se ejecutará cuando el usuario seleccione la nueva hora
                // hourOfDay y minute contienen la nueva hora seleccionada

                // Crear un objeto LocalDateTime con la fecha existente y la nueva hora y minutos
                val newTime = LocalDateTime.of(year, month, day, hourOfDay, minute)

                // Actualizar el tiempo en la tarea actual
                mTask.startTime = newTime

                // Actualizar la vista en el diálogo de detalle
                val tvTime = dialog!!.findViewById<TextView>(R.id.tv_start_time)
                tvTime.text = Utils.beautifyDate(newTime.format(Utils.getFullFormatter()))
            },
            currentHour,
            currentMinute,
            false
        )

        // Mostrar el diálogo del selector de tiempo
        timePickerDialog.show()
    }

    private fun updateTaskOnline(mTask: Task) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            val retrofit = Client.client

            // Crear una instancia del servicio de la API
            val apiService = retrofit?.create(
                Service::class.java
            )
            val tr = TaskRequest(mTask)

            // Utilizar el servicio para realizar llamadas a la API
            val call = apiService?.updateTask(
                PreferencesHandler.getToken(ctx), PreferencesHandler.getId(
                    ctx
                ), mTask.id, tr
            )

            // Ejecutar la llamada de forma asíncrona
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? = response.body()?.string()
                        if (responseBody == "OK") {
                            updateTaskFromDB(mTask)
                        } else {
                            Utils.showToast(
                                ctx, Utils.SUCCESS,
                                ctx.getString(R.string.update_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.update_error))
                    }
                    dialog!!.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.connection_error))
                }
            })
        } else {
            updateTaskFromDB(mTask)
        }
    }

    private fun updateTaskFromDB(mTask: Task) {
        val updatedTasks = DB.getInstance(ctx)!!.updateTask(mTask)
        if (updatedTasks == 0) {
            Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.updated_zero))
        } else {
            Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.update_success))
            Logger.i(TAG, "Tarea actualizada correctamente: $mTask")
        }
        notifyDataSetChanged()
        if (dialog!!.isShowing) dialog!!.dismiss()
    }

    private fun deleteTask(mTask: Task) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            val retrofit = Client.client

            // Crear una instancia del servicio de la API
            val apiService = retrofit!!.create(
                Service::class.java
            )

            // Utilizar el servicio para realizar llamadas a la API
            val call = apiService.deleteTask(
                PreferencesHandler.getToken(ctx), PreferencesHandler.getId(
                    ctx
                ), mTask.id
            )

            // Ejecutar la llamada de forma asíncrona
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? = response.body()?.string()
                        if (responseBody == "OK") {
                            deleteTaskFromDB(mTask)
                        } else {
                            Utils.showToast(
                                ctx, Utils.SUCCESS,
                                ctx.getString(R.string.delete_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.delete_error))
                    }
                    dialog!!.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.connection_error))
                }
            })
        } else {
            deleteTaskFromDB(mTask)
        }
    }

    private fun deleteTaskFromDB(mTask: Task) {
        val deletedTasks = DB.getInstance(ctx)!!
            .delete(DB.TASKS_TABLE, mTask.id)
        if (deletedTasks == 0) {
            Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.delete_zero))
        } else {
            // La nota se eliminó correctamente
            Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.delete_success))
            Logger.i(TAG, "Tarea eliminada correctamente: $mTask")
            taskList.remove(mTask)
            notifyDataSetChanged()
        }
        if (dialog!!.isShowing) dialog!!.dismiss()
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView

        init {
            tvTitle = itemView.findViewById(R.id.tv_title)
        }

        fun bindData(task: Task) {
            tvTitle.text = task.title
        }
    }

    companion object {
        private val TAG = TaskAdapter::class.java.simpleName
    }
}