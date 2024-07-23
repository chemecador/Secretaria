package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    val notesLists: LiveData<Resource<List<NotesList>>> = repository.getLists()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _updateStatus = MutableLiveData<Resource<Unit>>()
    val updateStatus: LiveData<Resource<Unit>> get() = _updateStatus

    fun createList(name: String) {
        viewModelScope.launch {
            val result = repository.createList(name)
            if (result is Resource.Error) {
                _error.postValue(result.message ?: "Error")
                Timber.e(result.message)
            }
        }
    }

    fun deleteList(listId: String) = repository.deleteList(listId)

    fun editList(updatedList: NotesList) {
        _updateStatus.postValue(Resource.Loading())
        repository.editList(updatedList)
            .addOnSuccessListener {
                _updateStatus.postValue(Resource.Success(null))
            }
            .addOnFailureListener { e ->
                _updateStatus.postValue(Resource.Error(e.localizedMessage ?: "Error"))
            }
    }

}