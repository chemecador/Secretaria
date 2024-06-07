package com.chemecador.secretaria.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.utils.Resource

interface OnlineRepository {
    fun getLists(): LiveData<Resource<List<NotesList>>>
    suspend fun createList(name: String): Resource<Unit>
    fun getNotes(listId: String): LiveData<Resource<List<Note>>>
    suspend fun createNote(listId: String, note: Note): Resource<Unit>
    fun getNote(listId: String, noteId: String): MutableLiveData<Resource<Note>>
}