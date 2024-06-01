package com.chemecador.secretaria.data.repositories

import androidx.lifecycle.LiveData
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.utils.Resource

interface OnlineRepository {
    fun getAllLists(userId: String): LiveData<Resource<List<NotesList>>>
    suspend fun createList(userId: String, name: String)
}