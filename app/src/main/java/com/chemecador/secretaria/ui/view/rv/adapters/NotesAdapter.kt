package com.chemecador.secretaria.ui.view.rv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.ListItemNoteBinding
import com.chemecador.secretaria.ui.view.rv.holders.NoteViewHolder
import com.chemecador.secretaria.utils.NoteDiffCallback
import timber.log.Timber

class NotesAdapter(
    private val onNoteClicked: (String) -> Unit,
    private var noteColor: Int = 0
) : ListAdapter<Note, NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, noteColor)
        holder.itemView.setOnClickListener {
            onNoteClicked(note.id)
        }
    }


    fun updateNoteColor(color: Int) {
        this.noteColor = color
        notifyDataSetChanged()
    }

    private fun removeItem(position: Int) {
        val currentList = currentList.toMutableList()
        if (currentList.size == 1) {
            currentList.clear()
        } else if (position < currentList.size && position >= 0) {
            currentList.removeAt(position)
        } else {
            Timber.e("Tried to delete item at position $position, but size is ${currentList.size}")
            return
        }
        submitList(currentList.toList())
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, currentList.size - position)
    }
}
