package com.chemecador.secretaria.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.fragments.NotesFragment
import com.chemecador.secretaria.interfaces.OnLongClickListener
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.network.retrofit.Client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ListAdapter(ctx: Context, notesLists: MutableList<NotesList>) :
    RecyclerView.Adapter<ListAdapter.ViewHolder?>(), OnLongClickListener {
    private var notesListsList: MutableList<NotesList>
    private val mInflater: LayoutInflater
    private val ctx: Context
    private var dialog: AlertDialog? = null
    private lateinit var longClickListener: OnLongClickListener

    init {
        this.mInflater = LayoutInflater.from(ctx)
        this.notesListsList = notesLists
        this.ctx = ctx
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = mInflater.inflate(R.layout.item_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = notesListsList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtener la tarea actual
        val mList: NotesList = notesListsList[position]
        holder.bindData(mList)
        // Asignar un listener de clic al elemento de la lista
        holder.itemView.setOnClickListener {
            val notesFragment = NotesFragment()
            val args = Bundle()
            args.putInt("listId", mList.id)
            notesFragment.arguments = args

            // Realizar la transacción del fragmento
            val fragmentManager: FragmentManager =
                (ctx as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    notesFragment
                ) // Reemplaza "R.id.container" con el ID de tu contenedor principal
                .addToBackStack(null) // Agrega el fragmento actual a la pila de retroceso
                .commit()
        }
        holder.itemView.setOnLongClickListener {
            longClickListener.onLongClick(position)
            true
        }
    }

    fun setOnLongClickListener(listener: OnLongClickListener) {
        longClickListener = listener
    }

    override fun onLongClick(position: Int) {
        showList(notesListsList[position])
    }

    private fun showList(mList: NotesList) {
        val builder = AlertDialog.Builder(ctx)
        val inflater: LayoutInflater = LayoutInflater.from(ctx)
        val dialogView: View = inflater.inflate(R.layout.detail_list, null)
        builder.setView(dialogView)
        val tilTitle: TextInputLayout = dialogView.findViewById(R.id.til_title)
        tilTitle.editText?.setText(mList.name)
        val cbCheck: MaterialCheckBox =
            dialogView.findViewById(R.id.cb_check_list)
        cbCheck.isChecked = mList.type == 1
        val switchPublic: SwitchMaterial =
            dialogView.findViewById(R.id.switch_public)
        switchPublic.isChecked = mList.privacy == 0
        val btnUpdate = dialogView.findViewById<Button>(R.id.btn_update)
        btnUpdate.setOnClickListener {
            val newTitle: String = tilTitle.editText?.text.toString()
            if (newTitle.isEmpty()) {
                tilTitle.error = ctx.getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            mList.name = newTitle
            mList.privacy = if (switchPublic.isChecked) NotesList.PUBLIC else NotesList.PRIVATE
            mList.type = if (cbCheck.isChecked) NotesList.CHECK_LIST else NotesList.NORMAL_LIST
            updateListOnline(mList)
        }
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener { deleteList(mList) }
        dialog = builder.show()
    }

    private fun updateListOnline(mList: NotesList) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit = Client.client

            // Crear una instancia del servicio de la API
            val apiService: Service = retrofit.create(Service::class.java)

            // Ejecutar la llamada de forma asíncrona
            apiService.updateList(
                PreferencesHandler.getToken(ctx),
                PreferencesHandler.getId(ctx),
                mList.id,
                mList
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody: String? =
                            response.body()?.toString()
                        if (responseBody == "OK") {
                            updateListFromDB(mList)
                        } else {
                            Utils.showToast(
                                ctx, ctx.getString(R.string.update_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(ctx, ctx.getString(R.string.update_error))
                    }
                    dialog!!.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, ctx.getString(R.string.connection_error))
                }
            })
        } else {
            updateListFromDB(mList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateListFromDB(mList: NotesList) {
        val updatedLists: Int = DB.getInstance(ctx).updateList(mList)
        if (updatedLists == 0) {
            Utils.showToast(ctx, ctx.getString(R.string.updated_zero))
        } else {
            Utils.showToast(ctx, ctx.getString(R.string.update_success))
            Logger.i("ListAdapter", "Lista actualizada correctamente: $mList")
        }
        notifyDataSetChanged()
        if (dialog!!.isShowing) dialog!!.dismiss()
    }

    private fun deleteList(mList: NotesList) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit = Client.client

            // Crear una instancia del servicio de la API
            val apiService: Service = retrofit.create(Service::class.java)

            // Utilizar el servicio para realizar llamadas a la API
            val call: Call<ResponseBody> = apiService.deleteList(
                PreferencesHandler.getToken(ctx), PreferencesHandler.getId(ctx), mList.id
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
                            deleteListFromDB(mList)
                        } else {
                            Utils.showToast(
                                ctx, ctx.getString(R.string.delete_error)
                            )
                        }
                    } else if (response.code() == 500 && response.errorBody()?.string()
                            ?.contains("FOREIGN KEY") == true
                    ) {
                        Utils.showToast(ctx, ctx.getString(R.string.error_list_not_empty))

                    } else {
                        Utils.showToast(ctx, ctx.getString(R.string.delete_error))
                    }
                    dialog!!.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, ctx.getString(R.string.connection_error))
                }
            })
        } else {
            deleteListFromDB(mList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteListFromDB(mList: NotesList) {
        val deletedLists: Int = DB.getInstance(ctx).delete(DB.LISTS, mList.id)
        if (deletedLists == 0) {
            Utils.showToast(ctx, ctx.getString(R.string.delete_zero))
        } else {
            // La nota se eliminó correctamente
            Utils.showToast(ctx, ctx.getString(R.string.delete_success))
            Logger.e("ListAdapter", "Lista eliminada correctamente: $mList")
        }
        notesListsList.remove(mList)
        notifyDataSetChanged()
       // updateList(notesListsList)

        if (dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    /*fun updateList(newList: List<NotesList>) {
        val listDiff = ListDiffUtil(notesListsList, newList)
        val result = DiffUtil.calculateDiff(listDiff)
        notesListsList = newList
        result.dispatchUpdatesTo(this)
    }*/

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbCheckList: CheckBox
        private val tvTitle: TextView
        private val ivPublic: ImageView

        init {
            cbCheckList = itemView.findViewById(R.id.cb_check_list)
            tvTitle = itemView.findViewById(R.id.tv_title)
            ivPublic = itemView.findViewById(R.id.iv_public)
        }

        fun bindData(list: NotesList) {
            cbCheckList.visibility = if (list.type == 0) View.INVISIBLE else View.VISIBLE
            cbCheckList.isChecked = list.type == 1
            tvTitle.text = list.name
            ivPublic.visibility =
                if (list.privacy == 0) View.VISIBLE else View.INVISIBLE
        }
    }
}