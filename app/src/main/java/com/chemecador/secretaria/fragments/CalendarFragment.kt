package com.chemecador.secretaria.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.activities.LoginActivity
import com.chemecador.secretaria.adapters.TaskAdapter
import com.chemecador.secretaria.network.retrofit.Client.client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.databinding.FragmentCalendarBinding
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.fragments.detail.TaskDetailFragment
import com.chemecador.secretaria.gui.CustomToast
import com.chemecador.secretaria.interfaces.OnItemClickListener
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.responses.IdResponse
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment(), OnItemClickListener {
    private var binding: FragmentCalendarBinding? = null
    private var btnDay: Button? = null
    private lateinit var taskList: MutableList<Task>
    private var taskAdapter: TaskAdapter? = null
    private lateinit var ctx: Context
    private lateinit var selectedDay: LocalDateTime


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        init()
        return binding!!.root
    }

    private fun init() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        btnDay = toolbar.findViewById(R.id.btn_day)
        btnDay?.visibility = View.VISIBLE
        btnDay?.setOnClickListener { changeDay() }
        taskList = DB.getInstance(ctx).getTasksByDay(LocalDateTime.now())
        taskAdapter = TaskAdapter(ctx, taskList)
        taskAdapter!!.onItemClickListener = this
        val rv = binding!!.root.findViewById<RecyclerView>(R.id.recycler_view)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(ctx)
        rv.adapter = taskAdapter

        // Obtener una referencia al ActionBar
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        setDay(year, month + 1, day)
        binding!!.fab.setOnClickListener { createTask() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun changeDay() {
        val datePicker = DatePicker(requireContext())
        val dialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                onDayChanged(
                    year,
                    monthOfYear,
                    dayOfMonth
                )
            },
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth
        )
        dialog.show()
    }

    private fun setDay(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val day = String.format(Locale.getDefault(), "%02d", dayOfMonth)
        val month = String.format(Locale.getDefault(), "%02d", monthOfYear)
        val newDay =
            if (Calendar.getInstance()[Calendar.YEAR] == year) "$day/$month" else "$day/$month/$year"
        selectedDay = LocalDateTime.of(year, monthOfYear, dayOfMonth, 0, 0, 0)
        btnDay!!.text = newDay
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDayChanged(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        setDay(year, monthOfYear + 1, dayOfMonth)
        taskList.clear()
        taskList.addAll(DB.getInstance(ctx).getTasksByDay(selectedDay))
        taskAdapter!!.notifyDataSetChanged()
    }

    private fun createTask() {
        val mTask = Task()
        mTask.startTime = selectedDay.toEpochSecond(ZoneOffset.UTC)
        val builder = AlertDialog.Builder(
            ctx
        )
        val inflater = LayoutInflater.from(ctx)
        val dialogView = inflater.inflate(R.layout.dialog_new_task, null)
        val editText = dialogView.findViewById<EditText>(R.id.et_title)
        val selectTime = dialogView.findViewById<RadioButton>(R.id.radio_select_time)
        val allDayLong = dialogView.findViewById<RadioButton>(R.id.radio_all_day_long)
        val cbContent = dialogView.findViewById<MaterialCheckBox>(R.id.cb_content)
        val etContent = dialogView.findViewById<EditText>(R.id.et_content)
        dialogView.findViewById<TextView>(R.id.tv_title).text = getString(R.string.insert_task_for, Utils.beautifyDate(selectedDay))
        val positiveButton = dialogView.findViewById<Button>(R.id.btn_ok)
        val negativeButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        builder.setView(dialogView)
        val dialog = builder.create()
        cbContent.addOnCheckedStateChangedListener { _: MaterialCheckBox?, state: Int ->
            if (state == 1) etContent.visibility = View.VISIBLE else etContent.visibility =
                View.GONE
        }
        selectTime.setOnClickListener {
            selectTime.isChecked = true
            allDayLong.isChecked = false
        }
        allDayLong.setOnClickListener {
            selectTime.isChecked = false
            allDayLong.isChecked = true
        }
        positiveButton.setOnClickListener {
            mTask.title = editText.text.toString()
            if (etContent.visibility == View.VISIBLE) {
                mTask.content = etContent.text.toString()
            } else {
                mTask.content = ""
            }
            if (allDayLong.isChecked) {
                if (PreferencesHandler.isOnline(ctx)) {

                    // Crear un objeto LocalDateTime con la fecha actual y la hora seleccionada
                    val selectedDateTime = LocalDateTime.of(
                        selectedDay.year,
                        selectedDay.month, selectedDay.dayOfMonth, 0, 0, 0
                    )

                    // Asignar el valor formateado al objeto mTask
                    mTask.startTime = selectedDateTime?.toEpochSecond(ZoneOffset.UTC)
                    syncTask(mTask)
                } else {
                    insertTask(mTask)
                }
            } else if (selectTime.isChecked) {
                askTime(mTask)
            }
            dialog.dismiss()
        }
        negativeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onItemClick(position: Int) {


        val taskDetailFragment = TaskDetailFragment()

        val task = taskList[position]
        val bundle = Bundle()
        bundle.putString(TaskDetailFragment.TITLE, task.title)
        bundle.putString(TaskDetailFragment.CONTENT, task.content)
        bundle.putInt(TaskDetailFragment.ID, task.id)
        task.startTime?.let { bundle.putLong(TaskDetailFragment.START_TIME, it) }
        taskDetailFragment.arguments = bundle

        // Obtener los datos del elemento en la posición 'position'
        val transaction = (ctx as FragmentActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, taskDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun askTime(mTask: Task) {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val timePickerDialog = TimePickerDialog(
            ctx,
            { _: TimePicker?, hourOfDay1: Int, minute1: Int ->

                // Crear un objeto LocalDateTime con la fecha actual y la hora seleccionada
                val selectedDateTime = LocalDateTime.of(
                    selectedDay.year,
                    selectedDay.month, selectedDay.dayOfMonth, hourOfDay1, minute1
                )


                // Asignar el valor formateado al objeto mTask
                mTask.startTime = selectedDateTime.toEpochSecond(ZoneOffset.UTC)
                if (PreferencesHandler.isOnline(ctx)) {
                    syncTask(mTask)
                } else {
                    insertTask(mTask)
                }
            },
            hourOfDay,
            minute,
            true // true para formato de 24 horas, false para formato de 12 horas
        )
        timePickerDialog.show()
    }

    private fun insertTask(mTask: Task) {
        // Agregar la tarea a la lista
        taskList.add(mTask)

        // Notificar al adaptador del cambio en la lista de tareas
        taskAdapter!!.notifyItemInserted(taskList.size - 1)


        // Almacenar la cadena en la base de datos SQLite
        DB.getInstance(ctx).insertTask(mTask)
        Snackbar.make(binding!!.root, getString(R.string.create_task_success), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.fab)
            .show()
        Logger.i(className, "Tarea creada correctamente: $mTask")
    }

    private fun syncTask(mTask: Task) {

        // Obtener la instancia de Retrofit
        val retrofit = client

        // Crear una instancia del servicio de la API
        val apiService = retrofit!!.create(
            Service::class.java
        )
        val userId = PreferenceManager.getDefaultSharedPreferences(ctx).getInt("id", -1)
        val token = PreferencesHandler.getToken(ctx)
        if (userId == -1) {
            CustomToast(ctx, Utils.ERROR, Toast.LENGTH_LONG).show(getString(R.string.login_again))
            (ctx as Activity?)!!.finish()
            startActivity(Intent(ctx, LoginActivity::class.java))
            return
        }

        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService.createTask(token, userId, mTask)

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<IdResponse?> {
            override fun onResponse(call: Call<IdResponse?>, response: Response<IdResponse?>) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result = response.body() ?: return
                    val id = result.id
                    mTask.id = id
                    insertTask(mTask)
                } else if (response.code() == 401) {
                    // Manejar el error de respuesta
                    Utils.showToast(
                        ctx,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.unauthorized)
                    )
                } else {
                    Utils.showToast(
                        ctx,
                        Utils.ERROR,
                        response.code().toString() + " : " + getString(R.string.server_error)
                    )
                }
            }

            override fun onFailure(call: Call<IdResponse?>, t: Throwable) {

                // Manejar el error de conexión o la excepción
                Utils.showToast(ctx, Utils.ERROR, getString(R.string.server_error))
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    companion object {
        private val className = CalendarFragment::class.java.simpleName
    }
}