package com.chemecador.secretaria.fragments.detail

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.api.Client
import com.chemecador.secretaria.api.Service
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskDetailFragment : Fragment() {

    private val className = this@TaskDetailFragment.javaClass.simpleName
    private lateinit var mTask : Task
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvStartTime: TextView

    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tasks_detail, container, false)

        etTitle = view.findViewById(R.id.et_title)
        etContent = view.findViewById(R.id.et_content)
        tvStartTime = view.findViewById(R.id.tv_start_time)

        btnUpdate = view.findViewById(R.id.btn_update)
        btnDelete = view.findViewById(R.id.btn_delete)

        // Recibir datos del Bundle
        val args = arguments
        if (args != null) {
            val id = args.getInt(ID, -1)
            val title = args.getString(TITLE, "")
            val content = args.getString(CONTENT, "")
            val startTime = args.getLong(START_TIME, 0)
            mTask = Task(id, title, content, startTime)

            etTitle.text = Editable.Factory.getInstance().newEditable(title)
            etContent.text = Editable.Factory.getInstance().newEditable(content)


            val instant = Instant.ofEpochSecond(startTime)

            // Convertir el Instant a una fecha y hora en una zona horaria específica (por ejemplo, UTC)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))

            // Formatear la fecha y hora en un formato legible por el usuario
            val formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))

            //tvStartTime.text = Editable.Factory.getInstance().newEditable(Utils.beautifyDate(formattedDateTime))
            tvStartTime.text = Editable.Factory.getInstance().newEditable(formattedDateTime)


        }

        btnUpdate.setOnClickListener {
            mTask.title = etTitle.text.toString()
            mTask.content = etContent.text.toString()
            updateTaskOnline(mTask)
        }
        btnDelete.setOnClickListener { deleteTaskOnline(mTask) }


        return view
    }




    private fun updateTaskOnline(mTask: Task) {
        if (PreferencesHandler.isOnline(requireContext())) {
            // Obtener la instancia de Retrofit
            val retrofit = Client.client

            // Crear una instancia del servicio de la API
            val apiService = retrofit?.create(
                Service::class.java
            )

            // Utilizar el servicio para realizar llamadas a la API
            val call = apiService?.updateTask(
                PreferencesHandler.getToken(requireContext()), PreferencesHandler.getId(
                    requireContext()
                ), mTask.id, mTask
            )

            // Ejecutar la llamada de forma asíncrona
            call!!.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? = response.body()?.string()
                        if (responseBody == "OK") {
                            updateTaskFromDB(mTask)
                        } else {
                            Utils.showToast(
                                requireContext(), Utils.SUCCESS,
                                requireContext().getString(R.string.update_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(requireContext(), Utils.ERROR, requireContext().getString(R.string.update_error))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(requireContext(), Utils.SUCCESS, requireContext().getString(R.string.connection_error))
                }
            })
        } else {
            updateTaskFromDB(mTask)
        }
    }

    private fun updateTaskFromDB(mTask: Task) {
        val updatedTasks = DB.getInstance(requireContext()).updateTask(mTask)
        if (updatedTasks == 0) {
            Utils.showToast(requireContext(), Utils.ERROR, requireContext().getString(R.string.updated_zero))
        } else {
            Utils.showToast(requireContext(), Utils.SUCCESS, requireContext().getString(R.string.update_success))
            Logger.i(className, "Tarea actualizada correctamente: $mTask")
        }
    }

    private fun deleteTaskOnline(mTask: Task) {
        if (PreferencesHandler.isOnline(requireContext())) {
            // Obtener la instancia de Retrofit
            val retrofit = Client.client

            // Crear una instancia del servicio de la API
            val apiService = retrofit!!.create(
                Service::class.java
            )

            // Utilizar el servicio para realizar llamadas a la API
            val call = apiService.deleteTask(
                PreferencesHandler.getToken(requireContext()), PreferencesHandler.getId(
                    requireContext()
                ), mTask.id
            )

            // Ejecutar la llamada de forma asíncrona
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? = response.body()?.string()
                        if (responseBody == "OK") {
                            deleteTaskFromDB(mTask)
                        } else {
                            Utils.showToast(
                                requireContext(), Utils.SUCCESS,
                                requireContext().getString(R.string.delete_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(requireContext(), Utils.ERROR, requireContext().getString(R.string.delete_error))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(requireContext(), Utils.SUCCESS, requireContext().getString(R.string.connection_error))
                }
            })
        } else {
            deleteTaskFromDB(mTask)
        }
    }

    private fun deleteTaskFromDB(mTask: Task) {
        val deletedTasks = DB.getInstance(requireContext())
            .delete(DB.TASKS, mTask.id)
        if (deletedTasks == 0) {
            Utils.showToast(requireContext(), Utils.ERROR, requireContext().getString(R.string.delete_zero))
        } else {
            // La nota se eliminó correctamente
            Utils.showToast(requireContext(), Utils.SUCCESS, requireContext().getString(R.string.delete_success))
            Logger.i(className, "Tarea eliminada correctamente: $mTask")
        }
        onBackPressed()
    }
    // Agrega este método para manejar el botón Atrás (Back)
    private fun onBackPressed() {

        // Reemplaza el fragmento actual con el fragmento anterior.
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.popBackStack()
    }

    companion object {
        const val TITLE = "title"
        const val CONTENT = "content"
        const val START_TIME = "start_time"
        const val ID = "id"
    }
}
