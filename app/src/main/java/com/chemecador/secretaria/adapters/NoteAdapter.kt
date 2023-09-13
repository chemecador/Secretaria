package com.chemecador.secretaria.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.interfaces.OnItemClickListener
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.logger.Logger
import com.google.android.material.checkbox.MaterialCheckBox

class NoteAdapter(ctx: Context, notes: MutableList<Note>?, isPublic: Boolean) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder?>() {
    private val noteList: MutableList<Note>?
    private val mInflater: LayoutInflater
    private val ctx: Context
    private var dialog: AlertDialog? = null
    private val isPublic: Boolean
    var onItemClickListener: OnItemClickListener? = null


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
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            onItemClickListener?.onItemClick(position)
        }
        holder.cb.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            DB.getInstance(
                ctx
            ).updateNoteStatus(mNote.id, isChecked)
        }
        holder.ivPublic.visibility = if (isPublic) View.VISIBLE else View.INVISIBLE
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