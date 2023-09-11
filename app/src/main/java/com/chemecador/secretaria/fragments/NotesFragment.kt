package com.chemecador.secretaria.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.activities.LoginActivity
import com.chemecador.secretaria.adapters.NoteAdapter
import com.chemecador.secretaria.api.Client.client
import com.chemecador.secretaria.api.Service
import com.chemecador.secretaria.databinding.FragmentNotesBinding
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.fragments.detail.NoteDetailFragment
import com.chemecador.secretaria.gui.CustomToast
import com.chemecador.secretaria.interfaces.OnItemClickListener
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.requests.NoteRequest
import com.chemecador.secretaria.responses.IdResponse
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotesFragment : Fragment(), OnItemClickListener {
    private var notes: MutableList<Note>? = null
    private lateinit var ctx: Context
    private var adapter: NoteAdapter? = null
    private var listId = 0
    private var binding: FragmentNotesBinding? = null
    private var isPublic = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        if (arguments != null) listId = requireArguments().getInt("listId")
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val btnDay = toolbar.findViewById<Button>(R.id.btn_day)
        btnDay.visibility = View.GONE
        init()
        return binding!!.root
    }

    private fun init() {
        notes = DB.getInstance(ctx).getNotesByList(listId)
        isPublic = DB.getInstance(ctx).getPrivacy(listId) == NotesList.PUBLIC
        val rvLists = binding!!.root.findViewById<RecyclerView>(R.id.rv)
        adapter = NoteAdapter(ctx, notes, isPublic)
        adapter!!.onItemClickListener = this
        rvLists.layoutManager = LinearLayoutManager(ctx)
        rvLists.adapter = adapter


        // Obtener una referencia al ActionBar
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val fab = binding!!.root.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { createNote() }
    }

    private fun createNote() {
        val builder = MaterialAlertDialogBuilder(ctx)
        val inflater = LayoutInflater.from(ctx)
        val dialogView = inflater.inflate(R.layout.dialog_new_note, null)
        val cbContent = dialogView.findViewById<MaterialCheckBox>(R.id.cb_content)
        val tilTitle = dialogView.findViewById<TextInputLayout>(R.id.til_title)
        val tilContent = dialogView.findViewById<TextInputLayout>(R.id.til_content)
        cbContent.addOnCheckedStateChangedListener { _: MaterialCheckBox?, state: Int ->
            if (state == 1) {
                tilContent.visibility = View.VISIBLE
                dialogView.findViewById<View>(R.id.tv_content).visibility = View.VISIBLE
            } else {
                tilContent.visibility = View.GONE
                dialogView.findViewById<View>(R.id.tv_content).visibility = View.GONE
            }
        }
        val positiveButton = dialogView.findViewById<Button>(R.id.btn_ok)
        val negativeButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        builder.setView(dialogView)
        val dialog = builder.show()
        positiveButton.setOnClickListener {
            val note = Note()
            note.title = tilTitle.editText!!.text.toString()
            note.listId = listId
            note.content = if (tilContent.visibility == View.VISIBLE) tilContent.editText!!
                .text.toString() else ""
            note.status = DB.getInstance(ctx).getType(listId)
            if (PreferencesHandler.isOnline(ctx)) {
                syncNote(note)
            } else {
                insertNote(note)
            }
            dialog.dismiss()
        }
        negativeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun syncNote(mNote: Note) {

        // Obtener la instancia de Retrofit
        val retrofit = client

        // Crear una instancia del servicio de la API
        val apiService = retrofit!!.create(
            Service::class.java
        )
        val userId = PreferenceManager.getDefaultSharedPreferences(
            ctx
        ).getInt("id", -1)
        val token = PreferencesHandler.getToken(ctx)
        if (userId == -1) {
            CustomToast(ctx, Utils.ERROR, Toast.LENGTH_LONG).show(getString(R.string.login_again))
            (ctx as Activity?)!!.finish()
            startActivity(Intent(ctx, LoginActivity::class.java))
            return
        }
        val nr = NoteRequest(mNote)
        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService.createNote(token, userId, mNote.listId, nr)

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<IdResponse?> {

            override fun onResponse(call: Call<IdResponse?>, response: Response<IdResponse?>) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result = response.body()!!
                    val id = result.id
                    mNote.id = id
                    insertNote(mNote)
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
                Utils.showToast(
                    ctx, Utils.ERROR, """${getString(R.string.server_error)} :
${t.message}"""
                )
            }
        })
    }

    private fun insertNote(mNote: Note) {
        DB.getInstance(ctx).insertNote(mNote)
        notes?.add(mNote)
        // Notificar al adaptador del cambio en la lista de tareas
        adapter!!.notifyItemInserted(notes!!.size - 1)
        Snackbar.make(binding!!.root, getString(R.string.create_note_success), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.fab)
            .show()
        Logger.i(className, "Nota insertada correctamente: $mNote")
    }


    override fun onItemClick(position: Int) {


        val noteDetailFragment = NoteDetailFragment()

        val note = notes?.get(position)
        val bundle = Bundle()
        if (note != null) {
            bundle.putString("title", note.title)
            bundle.putString("content", note.content)
            bundle.putInt("status", note.status)
            noteDetailFragment.arguments = bundle
        }

        // Obtener los datos del elemento en la posición 'position'
        val transaction = (ctx as FragmentActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, noteDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onDestroy() {
        super.onDestroy()

        // Restaurar la visibilidad del botón de retroceso al salir del fragmento
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    companion object {
        val className: String = NotesFragment::class.java.simpleName
    }
}