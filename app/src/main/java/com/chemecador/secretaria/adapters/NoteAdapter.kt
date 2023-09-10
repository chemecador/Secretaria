package com.chemecador.secretaria.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.api.Client
import com.chemecador.secretaria.api.Service
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.requests.NoteRequest
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.Objects

class NoteAdapter(ctx: Context, notes: MutableList<Note>?, isPublic: Boolean) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder?>() {
    private val noteList: MutableList<Note>?
    private val mInflater: LayoutInflater
    private val ctx: Context
    private var dialog: AlertDialog? = null
    private val isPublic: Boolean

    init {
        mInflater = LayoutInflater.from(ctx)
        noteList = notes
        this.ctx = ctx
        this.isPublic = isPublic
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = mInflater.inflate(R.layout.item_note, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return noteList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtener la tarea actual
        val mNote: Note = noteList!![position]
        holder.bindData(mNote)
        // Asignar un listener de clic al elemento de la lista
        holder.itemView.setOnClickListener { showNote(mNote) }
        holder.cb.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            DB.getInstance(
                ctx
            )!!.updateNoteStatus(mNote.id, isChecked)
        }
        holder.ivPublic.visibility = if (isPublic) View.VISIBLE else View.INVISIBLE
    }

    private fun showNote(mNote: Note) {
        val builder = AlertDialog.Builder(ctx)
        val inflater: LayoutInflater = LayoutInflater.from(ctx)
        val dialogView: View = inflater.inflate(R.layout.detail_note, null)
        builder.setView(dialogView)
        val tilTitle: TextInputLayout = dialogView.findViewById(R.id.til_title)
        val etContent: EditText = dialogView.findViewById(R.id.et_content)
        Objects.requireNonNull<EditText>(tilTitle.editText).setText(mNote.title)
        if (mNote.content?.isEmpty() == true) {
            etContent.visibility = View.GONE
        } else {
            etContent.setText(mNote.content)
            etContent.visibility = View.GONE
        }
        val btnUpdate = dialogView.findViewById<Button>(R.id.btn_update)
        btnUpdate.setOnClickListener {
            val newTitle: String = tilTitle.editText?.text.toString()
            val newContent: String = etContent.text.toString()
            if (newTitle.isEmpty()) {
                tilTitle.error = ctx.getString(R.string.error_empty_field)
                return@setOnClickListener
            }
            mNote.title = newTitle
            mNote.content = newContent
            updateNoteOnline(mNote)
        }
        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener { deleteNote(mNote) }
        dialog = builder.show()
    }

    private fun updateNoteOnline(mNote: Note) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit? = Client.client

            // Crear una instancia del servicio de la API
            val apiService: Service? = retrofit?.create(Service::class.java)
            val nr = NoteRequest(mNote)
            // Utilizar el servicio para realizar llamadas a la API
            val call: Call<ResponseBody>? = apiService?.updateNote(
                PreferencesHandler.getToken(ctx),
                PreferencesHandler.getId(ctx),
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
                                ctx, Utils.SUCCESS,
                                ctx.getString(R.string.update_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.update_error))
                    }
                    dialog!!.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.connection_error))
                }
            })
        } else {
            updateNoteFromDB(mNote)
        }
    }

    private fun updateNoteFromDB(mNote: Note) {
        val updatedNotes: Int = DB.getInstance(ctx)!!.updateNote(mNote)
        if (updatedNotes == 0) {
            Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.updated_zero))
        } else {
            Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.update_success))
            Logger.i(className, "Nota actualizada correctamente: $mNote")
        }
        notifyDataSetChanged()
        if (dialog!!.isShowing) dialog!!.dismiss()
    }

    private fun deleteNote(mNote: Note) {
        if (PreferencesHandler.isOnline(ctx)) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit? = Client.client

            // Crear una instancia del servicio de la API
            val apiService: Service? = retrofit?.create(Service::class.java)

            // Utilizar el servicio para realizar llamadas a la API
            val call: Call<ResponseBody>? = apiService?.deleteNote(
                PreferencesHandler.getToken(ctx),
                PreferencesHandler.getId(ctx),
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
                                ctx, Utils.SUCCESS,
                                ctx.getString(R.string.delete_error) + ": " + responseBody
                            )
                        }
                    } else {
                        Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.delete_error))
                    }
                    dialog!!.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Error en la llamada al servidor
                    Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.connection_error))
                }
            })
        } else {
            deleteNoteFromDB(mNote)
        }
    }

    private fun deleteNoteFromDB(mNote: Note) {
        val deletedNotes: Int = DB.getInstance(ctx)!!.delete(DB.NOTES, mNote.id)
        if (deletedNotes == 0) {
            Utils.showToast(ctx, Utils.ERROR, ctx.getString(R.string.delete_zero))
        } else {
            // La nota se eliminó correctamente
            Utils.showToast(ctx, Utils.SUCCESS, ctx.getString(R.string.delete_success))
            Logger.e(className, "Nota eliminada correctamente $mNote")
        }
        noteList!!.remove(mNote)
        notifyDataSetChanged()
        if (dialog!!.isShowing) dialog!!.dismiss()
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView
        val cb: MaterialCheckBox
        val ivPublic: ImageView

        init {
            tvTitle = itemView.findViewById(R.id.tv_title)
            cb = itemView.findViewById(R.id.cb_check_list)
            ivPublic = itemView.findViewById(R.id.iv_public)
        }

        fun bindData(note: Note) {
            tvTitle.text = note.title
            when (note.status) {
                0 ->                     // es una nota normal sin check list
                    cb.visibility = View.INVISIBLE

                1 -> {
                    // es una nota con check list pero sin terminar
                    cb.visibility = View.VISIBLE
                    cb.isChecked = false
                }

                2 -> {
                    // es una nota con checklist y terminada
                    cb.visibility = View.VISIBLE
                    cb.isChecked = true
                }

                else -> Logger.e("NoteAdapter", "El status de la nota es " + note.status)
            }
        }
    }

    companion object {
        val className: String = NoteAdapter::class.java.simpleName
    }
}