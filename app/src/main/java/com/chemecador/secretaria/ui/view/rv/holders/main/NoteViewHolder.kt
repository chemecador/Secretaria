package com.chemecador.secretaria.ui.view.rv.holders.main

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.ListItemNoteBinding
import com.chemecador.secretaria.utils.DateUtils


class NoteViewHolder(private val binding: ListItemNoteBinding) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(note: Note, color: Int) {
        binding.tvTitle.text = note.title
        binding.tvContent.text = note.content
        binding.tvContent.isVisible = note.content.isNotBlank()
        binding.tvDate.text = DateUtils.formatSimple(note.date)
        if (color != 0) {
            binding.cv.setBackgroundColor(color)
        }
    }

}