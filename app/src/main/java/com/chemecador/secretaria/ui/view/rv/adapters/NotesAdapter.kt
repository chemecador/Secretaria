package com.chemecador.secretaria.ui.view.rv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.ListItemNoteBinding
import com.chemecador.secretaria.ui.view.rv.holders.NoteViewHolder
import com.chemecador.secretaria.utils.NoteDiffCallback

class NotesAdapter : ListAdapter<Note, NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }
}
