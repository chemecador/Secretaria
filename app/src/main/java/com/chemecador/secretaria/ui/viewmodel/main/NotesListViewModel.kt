package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.repositories.Repository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    fun getNotesLists(userId: String): LiveData<Resource<List<NotesList>>> {
        return repository.getAllLists(userId)
    }
}