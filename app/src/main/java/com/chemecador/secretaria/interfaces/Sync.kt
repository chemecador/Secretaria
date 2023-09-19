package com.chemecador.secretaria.interfaces

import android.content.Context


interface ListSync {
    suspend fun syncLists(context: Context): Boolean
}

interface NotesSync {
    suspend fun syncNotes(context: Context): Boolean
}

interface TasksSync {
    suspend fun syncTasks(context: Context): Boolean
}
