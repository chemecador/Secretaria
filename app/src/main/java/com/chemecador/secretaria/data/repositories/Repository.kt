package com.chemecador.secretaria.data.repositories

import androidx.lifecycle.LiveData
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.utils.Resource

interface Repository {
    fun getAllLists(userId: String): LiveData<Resource<List<NotesList>>>
}