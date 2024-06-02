package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.repositories.OnlineRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: OnlineRepository
) : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getNotes(listId: String): LiveData<Resource<List<Note>>> {
        return repository.getNotes(listId)
    }

    fun createNote(listId: String, note: Note) {
        viewModelScope.launch {
            val result = repository.createNote(listId, note)
            if (result is Resource.Error) {
                _error.postValue(result.message ?: "Error")
            }
        }
    }
}