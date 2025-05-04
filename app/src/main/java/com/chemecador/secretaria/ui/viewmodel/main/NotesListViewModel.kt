package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import com.chemecador.secretaria.utils.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateStatus = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val updateStatus: StateFlow<Resource<Unit>> = _updateStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val deleteStatus: StateFlow<Resource<Unit>> = _deleteStatus.asStateFlow()

    private val _shareListStatus = MutableSharedFlow<Resource<Unit>>()
    val shareListStatus: SharedFlow<Resource<Unit>> = _shareListStatus

    private val _notesLists = MutableStateFlow<Resource<List<NotesList>>>(Resource.Loading())
    val notesLists: StateFlow<Resource<List<NotesList>>> = _notesLists.asStateFlow()

    private val _contributors = MutableStateFlow<Set<String>>(emptySet())
    val contributors: StateFlow<Set<String>> = _contributors.asStateFlow()

    init {
        viewModelScope.launch {
            _notesLists.value = Resource.Loading()
            val lists = repository.getLists()
            val sortedLists = lists.data?.sortedByDescending { it.date }
            _notesLists.value = Resource.Success(sortedLists ?: emptyList())
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            val result = repository.createList(name)
            if (result is Resource.Error) {
                _error.value = result.message ?: "Error"
            } else {
                _notesLists.value = repository.getLists()
            }
        }
    }

    fun sortNotes(option: SortOption) {
        val sortedList = when (option) {
            SortOption.NAME_ASC -> notesLists.value.data?.sortedBy { it.name }
            SortOption.NAME_DESC -> notesLists.value.data?.sortedByDescending { it.name }
            SortOption.DATE_ASC -> notesLists.value.data?.sortedBy { it.date }
            SortOption.DATE_DESC -> notesLists.value.data?.sortedByDescending { it.date }
        }
        _notesLists.value = Resource.Success(sortedList ?: emptyList())
    }

    fun loadContributors(listId: String) {
        viewModelScope.launch {
            repository.getContributors(listId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val ids: List<String> = resource.data.orEmpty()
                            _contributors.value = ids.toSet()
                        }
                        else -> { /* Loading & Error: do nothing */}
                    }
                }
        }
    }

    fun unshareList(listId: String, friendId: String) {
        viewModelScope.launch {
            _shareListStatus.emit(Resource.Loading())
            val result = repository.unshareList(listId, friendId)
            _shareListStatus.emit(result)
            if (result is Resource.Success) loadContributors(listId)
        }
    }

    fun shareList(listId: String, friendId: String) {
        viewModelScope.launch {
            _shareListStatus.emit(Resource.Loading())
            val result = repository.shareList(listId, friendId)
            _shareListStatus.emit(result)
        }
    }

    fun editList(updatedList: NotesList) {
        _updateStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.editList(updatedList)
            _updateStatus.value = result
            if (result is Resource.Success) {
                _notesLists.value = repository.getLists()
            }
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            _deleteStatus.value = Resource.Loading()
            val result = repository.deleteList(listId)
            _deleteStatus.value = result
            if (result is Resource.Success) {
                _notesLists.value = repository.getLists()
            }
        }
    }
}