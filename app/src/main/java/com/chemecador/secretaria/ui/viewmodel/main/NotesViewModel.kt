package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import com.chemecador.secretaria.utils.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: MainRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _notes = MutableLiveData<Resource<List<Note>>>()
    val notes: LiveData<Resource<List<Note>>> get() = _notes

    fun getNotes(listId: String) {
        viewModelScope.launch {
            _notes.postValue(Resource.Loading())
            val result = repository.getNotes(listId)
            if (result is Resource.Success) {
                val sortedNotes = result.data?.sortedByDescending { it.date }
                _notes.postValue(Resource.Success(sortedNotes ?: emptyList()))
            } else {
                _notes.postValue(result)
            }
        }
    }


    fun createNote(listId: String, note: Note) {
        viewModelScope.launch {
            val result = repository.createNote(listId, note)
            if (result is Resource.Error) {
                _error.postValue(result.message ?: "Error")
            } else {
                _notes.postValue(repository.getNotes(listId))
            }
        }
    }

    fun sortNotes(option: SortOption) {
        val currentNotes = _notes.value?.data ?: return
        val sortedNotes = when (option) {
            SortOption.NAME_ASC -> currentNotes.sortedBy { it.title }
            SortOption.NAME_DESC -> currentNotes.sortedByDescending { it.title }
            SortOption.DATE_ASC -> currentNotes.sortedBy { it.date }
            SortOption.DATE_DESC -> currentNotes.sortedByDescending { it.date }
        }
        _notes.postValue(Resource.Success(sortedNotes))
    }


    fun getUsername() = userRepository.getUsername() ?: ""

}
