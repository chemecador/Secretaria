package com.chemecador.secretaria.ui.view.rv.holders

import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.databinding.ListItemNoteslistBinding


class NotesListViewHolder(private val binding: ListItemNoteslistBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(notesList: NotesList, onListClicked: (String) -> Unit) {
        binding.tvTitle.text = notesList.name

        binding.root.setOnClickListener {
            onListClicked(notesList.id)
        }
    }
}