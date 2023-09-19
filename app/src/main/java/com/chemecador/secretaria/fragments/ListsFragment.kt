package com.chemecador.secretaria.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chemecador.secretaria.R
import com.chemecador.secretaria.activities.LoginActivity
import com.chemecador.secretaria.adapters.ListAdapter
import com.chemecador.secretaria.databinding.FragmentListsBinding
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.gui.CustomToast
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.network.retrofit.Client.client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.network.sync.SyncLists
import com.chemecador.secretaria.responses.IdResponse
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListsFragment : Fragment() {
    private lateinit var binding: FragmentListsBinding
    private lateinit var ctx: Context
    private var rvLists: RecyclerView? = null
    private var adapter: ListAdapter? = null
    private var lists: MutableList<NotesList>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListsBinding.inflate(inflater, container, false)
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        val btnDay = toolbar.findViewById<Button>(R.id.btn_day)
        btnDay.visibility = View.GONE
        init()
        return binding.root
    }

    private fun init() {
        lists = DB.getInstance(ctx).lists
        rvLists = binding.root.findViewById(R.id.rv)
        rvLists?.layoutManager = LinearLayoutManager(ctx)
        adapter = ListAdapter(ctx, lists)
        adapter!!.setOnLongClickListener(adapter)
        rvLists?.adapter = adapter

        // Obtener una referencia al ActionBar
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        binding.fab.setOnClickListener { createList() }

        val swipeRefreshLayout = binding.root.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Aquí puedes realizar la lógica de actualización de la interfaz
            // Por ejemplo, cargar nuevamente los datos de la lista
            SyncLists.getLists(ctx) { success ->
                swipeRefreshLayout.isRefreshing = false
                if (success) {
                    Toast.makeText(ctx, R.string.update_success, Toast.LENGTH_SHORT).show()
                } else {
                    Utils.showToast(ctx, CustomToast.TOAST_ERROR, R.string.update_error)
                }
            }
        }

    }

    private fun createList() {
        val builder = MaterialAlertDialogBuilder(ctx)

        // Inflar la vista personalizada
        val inflater = LayoutInflater.from(ctx)
        val dialogView = inflater.inflate(R.layout.dialog_new_list, null)
        builder.setView(dialogView)
        val switchPublic = dialogView.findViewById<SwitchMaterial>(R.id.switch_public)
        val cbCheck = dialogView.findViewById<MaterialCheckBox>(R.id.cb_check_list)
        val ivInfo = dialogView.findViewById<ImageView>(R.id.iv_info)
        if (PreferencesHandler.isOnline(ctx)) {
            ivInfo.visibility = View.VISIBLE
            switchPublic.visibility = View.VISIBLE
        } else {
            ivInfo.visibility = View.GONE
            switchPublic.visibility = View.GONE
        }
        val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.til_list_name)
        val dialog = builder.show()
        ivInfo.setOnClickListener { showInfo() }

        // Obtener los botones del diálogo después de mostrarlo para poder configurar sus acciones
        val positiveButton = dialogView.findViewById<Button>(R.id.btn_ok)
        positiveButton.setOnClickListener {
            if (textInputLayout.editText == null) return@setOnClickListener
            val listName = textInputLayout.editText!!.text.toString()
            if (TextUtils.isEmpty(listName) || listName.trim { it <= ' ' } == "") {
                // El campo está vacío, mostrar un Snackbar con el mensaje de error
                textInputLayout.error = getString(R.string.error_empty_field)
            } else {
                textInputLayout.error = null
                val db = DB.getInstance(
                    ctx
                )
                if (!db.existsList(listName)) {
                    // si es offline, es privada por defecto. Si es online, hay que mirar el switch.
                    var isPublic =
                        if (PreferencesHandler.isOnline(ctx)) NotesList.PUBLIC else NotesList.PRIVATE
                    if (isPublic == NotesList.PUBLIC) isPublic =
                        if (switchPublic.isChecked) NotesList.PUBLIC else NotesList.PRIVATE
                    val mList = NotesList(
                        null,
                        listName,
                        isPublic,
                        if (cbCheck.isChecked) NotesList.CHECK_LIST else NotesList.NORMAL_LIST
                    )
                    if (PreferencesHandler.isOnline(ctx)) {
                        syncList(mList)
                    } else {
                        insertList(mList)
                    }
                    dialog.dismiss()
                } else {
                    Utils.showToast(ctx, Utils.WARNING, getString(R.string.list_already_exists))
                }
            }
        }
        val negativeButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        negativeButton.setOnClickListener {
            // Acción al pulsar el botón negativo
            dialog.dismiss()
        }
    }

    private fun showInfo() {
        val builder = MaterialAlertDialogBuilder(ctx)

        // Inflar la vista personalizada
        val inflater = LayoutInflater.from(ctx)
        val dialogView = inflater.inflate(R.layout.dialog_list_info, null)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok, null)
        builder.show()
    }

    private fun insertList(mList: NotesList) {
        val listId = DB.getInstance(ctx).insertList(mList)
        if (!PreferencesHandler.isOnline(ctx)) mList.id = listId
        lists!!.add(mList)
        adapter!!.notifyItemInserted(lists!!.size - 1)
        Snackbar.make(binding.root, getString(R.string.create_list_success), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.fab)
            .show()
        Logger.i(className, "Lista insertada correctamente: $mList")
    }

    private fun syncList(mList: NotesList) {

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
        // Utilizar el servicio para realizar llamadas a la API
        val call = apiService.createList(token, userId, mList)

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(object : Callback<IdResponse?> {
            override fun onResponse(call: Call<IdResponse?>, response: Response<IdResponse?>) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result = response.body() ?: return
                    val id = result.id
                    mList.id = id
                    insertList(mList)
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
        private val className = ListsFragment::class.java.simpleName
    }
}