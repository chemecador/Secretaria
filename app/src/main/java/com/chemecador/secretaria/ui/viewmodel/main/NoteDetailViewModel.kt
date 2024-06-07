package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.repositories.FirestoreRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    fun getNoteById(listId: String, noteId: String): LiveData<Resource<Note>> {
        return repository.getNote(listId, noteId)
    }
}
