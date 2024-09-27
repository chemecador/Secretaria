package com.chemecador.secretaria.ui.view.rv.holders.main

import android.graphics.Paint
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
        binding.cb.isChecked = note.completed
        binding.cb.setOnCheckedChangeListener { _, isChecked ->
            setPaintFlags(isChecked)
        }
        setPaintFlags(note.completed)
    }

    private fun setPaintFlags(completed: Boolean) {

        val paintFlags = if (completed) {
            binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        binding.tvTitle.paintFlags = paintFlags
        binding.tvContent.paintFlags = paintFlags

    }
}