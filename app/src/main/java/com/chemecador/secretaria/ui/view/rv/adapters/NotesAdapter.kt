package com.chemecador.secretaria.ui.view.rv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.ListItemNoteBinding
import com.chemecador.secretaria.ui.view.rv.holders.NoteViewHolder
import com.chemecador.secretaria.utils.NoteDiffCallback
import timber.log.Timber

class NotesAdapter : ListAdapter<Note, NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
        holder.ivMore.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                showPopupMenu(holder.ivMore, position)
            }
        }
    }


    private fun showPopupMenu(view: View, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_note)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.option_edit -> {
                    Toast.makeText(view.context, "Jaja no puedes editar", Toast.LENGTH_SHORT).show()
                    //editItem(position)
                    true
                }

                R.id.option_delete -> {
                    removeItem(position)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun editItem(position: Int) {
        // TODO
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
