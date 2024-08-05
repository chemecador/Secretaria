package com.chemecador.secretaria.data.repositories.main

import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.utils.Resource

interface MainRepository {
    suspend fun getLists(): Resource<List<NotesList>>
    suspend fun createList(name: String): Resource<Unit>
    suspend fun getNotes(listId: String): Resource<List<Note>>
    suspend fun createNote(listId: String, note: Note): Resource<Unit>
    suspend fun getNote(listId: String, noteId: String): Resource<Note>
    suspend fun deleteNote(listId: String, noteId: String): Resource<Unit>
    suspend fun editNote(listId: String, note: Note): Resource<Unit>
    suspend fun deleteList(listId: String): Resource<Unit>
    suspend fun editList(updatedList: NotesList): Resource<Unit>
    suspend fun addContributorToList(listId: String, friendId: String): Resource<Unit>
}
