package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.repositories.OnlineRepository
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: OnlineRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val userId = MutableLiveData<String?>()

    init {
        viewModelScope.launch {
            userRepository.userId.collect { id ->
                userId.postValue(id)
            }
        }
    }

    val notesLists: LiveData<Resource<List<NotesList>>> = userId.switchMap { id ->
        if (id.isNullOrBlank()) {
            MutableLiveData(Resource.Error("User ID is null or blank"))
        } else {
            repository.getAllLists(id)
        }
    }
}