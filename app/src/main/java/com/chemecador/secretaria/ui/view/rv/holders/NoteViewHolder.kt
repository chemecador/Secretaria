package com.chemecador.secretaria.ui.view.rv.holders

import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.ListItemNoteBinding


class NoteViewHolder(private val binding: ListItemNoteBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val ivMore: ImageView = binding.ivMore
    fun bind(note: Note) {
        binding.tvTitle.text = note.title
        binding.tvContent.text = note.content
        binding.tvContent.isVisible = note.content.isNotBlank()
    }
}