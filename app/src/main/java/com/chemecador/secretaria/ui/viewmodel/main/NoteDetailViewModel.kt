package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _updateStatus = MutableLiveData<Resource<Void>>()
    val updateStatus: LiveData<Resource<Void>> = _updateStatus

    private val _deleteStatus = MutableLiveData<Resource<Void>>()
    val deleteStatus: LiveData<Resource<Void>> = _deleteStatus

    fun getNote(listId: String, noteId: String): LiveData<Resource<Note>> {
        return repository.getNote(listId, noteId)
    }

    fun deleteNote(listId: String, noteId: String) {
        _deleteStatus.postValue(Resource.Loading())
        repository.deleteNote(listId, noteId).addOnSuccessListener {
            _deleteStatus.postValue(Resource.Success())
        }.addOnFailureListener { e ->
            _deleteStatus.postValue(Resource.Error(e.localizedMessage ?: "Error"))
        }
    }

    fun editNote(listId: String, note: Note) {
        _updateStatus.postValue(Resource.Loading())
        repository.editNote(listId, note)
            .addOnSuccessListener {
                _updateStatus.postValue(Resource.Success(null))
            }
            .addOnFailureListener { e ->
                _updateStatus.postValue(Resource.Error(e.localizedMessage ?: "Error"))
            }
    }
}
