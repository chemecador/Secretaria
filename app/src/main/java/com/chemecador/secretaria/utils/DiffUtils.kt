package com.chemecador.secretaria.utils

import androidx.recyclerview.widget.DiffUtil
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList


class NotesListDiffCallback : DiffUtil.ItemCallback<NotesList>() {
    override fun areItemsTheSame(oldItem: NotesList, newItem: NotesList): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NotesList, newItem: NotesList): Boolean {
        return oldItem == newItem
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}
