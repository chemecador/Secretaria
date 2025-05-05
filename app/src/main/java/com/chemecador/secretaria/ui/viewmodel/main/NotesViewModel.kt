package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: MainRepository,
    private val userRepository: UserRepository,
    private val res: ResourceProvider
) : ViewModel() {

    private val _notes = MutableStateFlow<Resource<List<Note>>>(Resource.Loading())
    val notes: StateFlow<Resource<List<Note>>> = _notes.asStateFlow()

    private val _error = MutableSharedFlow<String>(replay = 0)
    val error: SharedFlow<String> = _error.asSharedFlow()

    fun getNotes(listId: String) {
        viewModelScope.launch {
            _notes.value = Resource.Loading()
            when (val result = repository.getNotes(listId)) {
                is Resource.Success -> {
                    val sorted = result.data.orEmpty()
                        .sortedByDescending { it.date }
                    _notes.value = Resource.Success(sorted)
                }

                is Resource.Error -> {
                    _notes.value = result
                    _error.emit(result.message ?: res.getString(R.string.error_unknown))
                }

                else -> { } /* Resource.Loading: do nothing */

            }
        }
    }

    fun createNote(listId: String, note: Note) {
        viewModelScope.launch {
            when (val result = repository.createNote(listId, note)) {
                is Resource.Success -> {
                    getNotes(listId)
                }

                is Resource.Error -> {
                    _error.emit(result.message ?: res.getString(R.string.error_creating_note))
                }

                else -> { } /* Resource.Loading: do nothing */
            }
        }
    }

    fun getUsername(): String = userRepository.getUsername() ?: ""
}
