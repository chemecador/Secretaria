package com.chemecador.secretaria.adapters.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.chemecador.secretaria.items.NotesList

class ListDiffUtil (
    private val oldList: List<NotesList>,
    private val newList: List<NotesList>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}