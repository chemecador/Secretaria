package com.chemecador.secretaria.ui.view.rv.holders

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.databinding.ListItemNoteslistBinding


class NotesListViewHolder(private val binding: ListItemNoteslistBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val ivMore: ImageView = binding.ivMore

    fun bind(notesList: NotesList, onListClick: (String, String) -> Unit) {
        binding.tvTitle.text = notesList.name
        binding.root.setOnClickListener {
            onListClick(notesList.id, notesList.name)
        }
    }
}