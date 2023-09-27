package com.chemecador.secretaria.fragments.detail

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.adapters.NoteAdapter
import com.chemecador.secretaria.network.retrofit.Client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.requests.NoteRequest
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class NoteDetailFragment : Fragment() {

    private lateinit var mNote : Note
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var tvStatus: TextView
    private lateinit var cbStatus: CheckBox

    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notes_detail, container, false)

        etTitle = view.findViewById(R.id.et_title)
        etContent = view.findViewById(R.id.et_content)
        tvStatus = view.findViewById(R.id.tv_status)
        cbStatus = view.findViewById(R.id.cb_status)

        btnUpdate = view.findViewById(R.id.btn_update)
        btnDelete = view.findViewById(R.id.btn_delete)

        // Recibir datos del Bundle
        val args = arguments
        if (args != null) {
            val listId = args.getInt(LIST_ID, -1)
            val id = args.getInt(ID, -1)
            val title = args.getString(TITLE, "")
            val content = args.getString(CONTENT, "")
            val status = args.getInt(STATUS, -1)
            mNote = Note(listId, id, title, content, status)

            etTitle.text = Editable.Factory.getInstance().newEditable(title)
            etContent.text = Editable.Factory.getInstance().newEditable(content)

            tvStatus.text = when (status) {
                1 -> {
                    getString(R.string.not_finished)
                }
                2 -> {
                    getString(R.string.finished)
                }
                else -> {
                    ""
                }
            }
        }
        if (tvStatus.text.isEmpty()) {
            cbStatus.visibility = View.GONE
        }
        cbStatus.isChecked = mNote.status == 2
        cbStatus.setOnCheckedChangeListener { _, b ->
            val statusText = if (b) getString(R.string.finished) else getString(R.string.not_finished)
            tvStatus.text = statusText
        }

        btnUpdate.setOnClickListener {
            mNote.title = etTitle.text.toString()
            mNote.content = etContent.text.toString()
            if (mNote.status > 0) mNote.status = if (cbStatus.isChecked) 2 else 1
            updateNoteOnline(mNote)
        }
        btnDelete.setOnClickListener { deleteNoteOnline(mNote) }


        return view
    }



    private fun updateNoteOnline(mNote: Note?) {
        if (mNote == null) return
        if (PreferencesHandler.isOnline(requireContext())) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit? = Client.client

            // Crear una instancia del servicio de la API
            val apiService: Service? = retrofit?.create(Service::class.java)
            val nr = NoteRequest(mNote)
            // Utilizar el servicio para realizar llamadas a la API
            val call: Call<ResponseBody>? = apiService?.updateNote(
                PreferencesHandler.getToken(requireContext()),
                PreferencesHandler.getId(requireContext()),
                mNote.listId,
                mNote.id,
                nr
            )

            // Ejecutar la llamada de forma asíncrona
            call?.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? =
                            response.body()?.string()
                        if (responseBody == "OK") {
                            updateNoteFromDB(mNote)
                        } else {
                            Utils.showToast(
                                requireContext(), requireContext().getString(R.string.update_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(
                            requireContext(),
                            requireContext().getString(R.string.update_error)
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(
                        requireContext(),
                        requireContext().getString(R.string.connection_error)
                    )
                }
            })
        } else {
            updateNoteFromDB(mNote)
        }
    }

    private fun updateNoteFromDB(mNote: Note) {
        val updatedNotes: Int = DB.getInstance(requireContext()).updateNote(mNote)
        if (updatedNotes == 0) {
            Utils.showToast(requireContext(), requireContext().getString(R.string.updated_zero))
        } else {
            Utils.showToast(requireContext(), requireContext().getString(R.string.update_success))
            Logger.i(NoteAdapter.className, "Nota actualizada correctamente: $mNote")
        }
    }

    private fun deleteNoteOnline(mNote: Note) {
        if (PreferencesHandler.isOnline(requireContext())) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit? = Client.client

            // Crear una instancia del servicio de la API
            val apiService: Service? = retrofit?.create(Service::class.java)

            // Utilizar el servicio para realizar llamadas a la API
            val call: Call<ResponseBody>? = apiService?.deleteNote(
                PreferencesHandler.getToken(requireContext()),
                PreferencesHandler.getId(requireContext()),
                mNote.listId,
                mNote.id
            )

            // Ejecutar la llamada de forma asíncrona
            call?.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? =
                            response.body()?.string()
                        if (responseBody == "OK") {
                            deleteNoteFromDB(mNote)
                        } else {
                            Utils.showToast(
                                requireContext(), requireContext().getString(R.string.delete_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(
                            requireContext(),
                            requireContext().getString(R.string.delete_error)
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(
                        requireContext(),
                        requireContext().getString(R.string.connection_error)
                    )
                }
            })
        } else {
            deleteNoteFromDB(mNote)
        }
    }

    private fun deleteNoteFromDB(mNote: Note) {
        val deletedNotes: Int = DB.getInstance(requireContext()).delete(DB.NOTES, mNote.id)
        if (deletedNotes == 0) {
            Utils.showToast(requireContext(), requireContext().getString(R.string.delete_zero))
        } else {
            // La nota se eliminó correctamente
            Utils.showToast(requireContext(), requireContext().getString(R.string.delete_success))
            Logger.e(NoteAdapter.className, "Nota eliminada correctamente $mNote")
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
        const val STATUS = "status"
        const val LIST_ID = "list_id"
        const val ID = "id"
    }
}
