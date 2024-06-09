package com.chemecador.secretaria.ui.view.rv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.databinding.ListItemNoteslistBinding
import com.chemecador.secretaria.ui.view.rv.holders.NotesListViewHolder
import com.chemecador.secretaria.utils.NotesListDiffCallback

class NotesListAdapter(
    private val onListClick: (String, String) -> Unit,
    private val onEditList: (String) -> Unit,
    private val onDeleteList: (String) -> Unit
) : ListAdapter<NotesList, NotesListViewHolder>(NotesListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemNoteslistBinding.inflate(layoutInflater, parent, false)
        return NotesListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesListViewHolder, position: Int) {
        val notesList = getItem(position)
        holder.bind(notesList, onListClick)
        holder.ivMore.setOnClickListener {
            showPopupMenu(it, position)
        }
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_edit_delete)
        popup.setOnMenuItemClickListener { item ->
            val notesList = getItem(position)
            when (item.itemId) {
                R.id.option_edit -> {
                    onEditList(notesList.id)
                    true
                }

                R.id.option_delete -> {
                    onDeleteList(notesList.id)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }
}
