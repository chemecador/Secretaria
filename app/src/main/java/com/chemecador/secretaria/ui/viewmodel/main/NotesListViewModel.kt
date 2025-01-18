package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import com.chemecador.secretaria.utils.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _updateStatus = MutableLiveData<Resource<Unit>>()
    val updateStatus: LiveData<Resource<Unit>> get() = _updateStatus

    private val _deleteStatus = MutableLiveData<Resource<Unit>>()
    val deleteStatus: LiveData<Resource<Unit>> get() = _deleteStatus

    private val _shareListStatus = MutableLiveData<Resource<Unit>>()
    val shareListStatus: LiveData<Resource<Unit>> get() = _shareListStatus

    private val _notesLists = MutableLiveData<Resource<List<NotesList>>>()
    val notesLists: LiveData<Resource<List<NotesList>>> get() = _notesLists

    init {
        viewModelScope.launch {
            _notesLists.postValue(Resource.Loading())
            val lists = repository.getLists()
            val sortedLists = lists.data?.sortedByDescending { it.date }
            _notesLists.postValue(Resource.Success(sortedLists ?: emptyList()))
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            val result = repository.createList(name)
            if (result is Resource.Error) {
                _error.postValue(result.message ?: "Error")
            } else {
                _notesLists.postValue(repository.getLists())
            }
        }
    }

    fun sortNotes(option: SortOption) {
        val sortedList = when (option) {
            SortOption.BY_NAME_ASC -> notesLists.value?.data?.sortedBy { it.name }
            SortOption.BY_NAME_DESC -> notesLists.value?.data?.sortedByDescending { it.name }
            SortOption.BY_DATE_ASC -> notesLists.value?.data?.sortedBy { it.date }
            SortOption.BY_DATE_DESC -> notesLists.value?.data?.sortedByDescending { it.date }
        }
        _notesLists.value = Resource.Success(sortedList ?: emptyList())
    }


    fun shareListWithFriend(listId: String, friendId: String) {
        viewModelScope.launch {
            _shareListStatus.value = Resource.Loading()
            val result = repository.addContributorToList(listId, friendId)
            _shareListStatus.value = result
        }
    }

    fun editList(updatedList: NotesList) {
        _updateStatus.postValue(Resource.Loading())
        viewModelScope.launch {
            val result = repository.editList(updatedList)
            _updateStatus.postValue(result)
            if (result is Resource.Success) {
                _notesLists.postValue(repository.getLists())
            }
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            _deleteStatus.postValue(Resource.Loading())
            val result = repository.deleteList(listId)
            _deleteStatus.postValue(result)
            if (result is Resource.Success) {
                _notesLists.postValue(repository.getLists())
            }
        }
    }
}