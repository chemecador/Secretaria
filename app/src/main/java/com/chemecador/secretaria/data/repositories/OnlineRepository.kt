package com.chemecador.secretaria.data.repositories

import androidx.lifecycle.LiveData
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.utils.Resource

interface OnlineRepository {
    fun getAllLists(): LiveData<Resource<List<NotesList>>>
    suspend fun createList(name: String): Resource<Unit>
    fun getNotes(listId: String): LiveData<Resource<List<Note>>>
}