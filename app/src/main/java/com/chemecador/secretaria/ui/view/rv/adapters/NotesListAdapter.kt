package com.chemecador.secretaria.ui.view.rv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.databinding.ListItemNoteslistBinding
import com.chemecador.secretaria.ui.view.rv.holders.NotesListViewHolder
import com.chemecador.secretaria.utils.NotesListDiffCallback

class NotesListAdapter(
    private val onListClick: (String, String) -> Unit
) : ListAdapter<NotesList, NotesListViewHolder>(NotesListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemNoteslistBinding.inflate(layoutInflater, parent, false)
        return NotesListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesListViewHolder, position: Int) {
        val notesList = getItem(position)
        holder.bind(notesList, onListClick)
    }

}
