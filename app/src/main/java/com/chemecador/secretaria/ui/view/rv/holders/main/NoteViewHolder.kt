package com.chemecador.secretaria.ui.view.rv.holders.main

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.ListItemNoteBinding
import com.chemecador.secretaria.utils.DateUtils


class NoteViewHolder(private val binding: ListItemNoteBinding) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(note: Note) {
        binding.tvTitle.text = note.title
        binding.tvContent.text = note.content
        binding.tvContent.isVisible = note.content.isNotBlank()
        binding.tvCreator.text = note.creator
        binding.tvDate.text = DateUtils.formatSimple(note.date)
        binding.cv.setBackgroundColor(note.color)

    }

}