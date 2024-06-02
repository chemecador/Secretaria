package com.chemecador.secretaria.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val res: ResourceProvider
) : OnlineRepository {

    companion object {
        private const val USERS = "users"
        private const val NOTES_LIST = "noteslist"
        private const val NOTES = "notes"
    }

    private fun getUserId(): String? {
        return runBlocking {
            userRepository.userId.first()
        }
    }

    override fun getAllLists(): LiveData<Resource<List<NotesList>>> {
        val liveData = MutableLiveData<Resource<List<NotesList>>>()
        liveData.postValue(Resource.Loading())

        try {
            val userId = getUserId()
            if (userId == null) {
                Timber.e("UserID is null ??")
                liveData.postValue(Resource.Error(res.getString(R.string.error_unknown)))
                return liveData
            }
            firestore.collection(USERS).document(userId).collection(NOTES_LIST)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        liveData.postValue(
                            Resource.Error(
                                error?.message ?: res.getString(R.string.error_unknown)
                            )
                        )
                    } else {
                        val lists = snapshot.documents.mapNotNull { documentSnapshot ->
                            documentSnapshot.toObject(NotesList::class.java)?.apply {
                                id = documentSnapshot.id
                            }
                        }
                        liveData.postValue(Resource.Success(lists))
                    }
                }
        } catch (e: Exception) {
            liveData.postValue(Resource.Error(e.message ?: res.getString(R.string.error_unknown)))
        }
        return liveData
    }

    override fun getNotes(listId: String): LiveData<Resource<List<Note>>> {
        val liveData = MutableLiveData<Resource<List<Note>>>()
        liveData.postValue(Resource.Loading())

        try {
            val userId = getUserId()
            if (userId == null) {
                Timber.e("UserID is null ??")
                liveData.postValue(Resource.Error(res.getString(R.string.error_unknown)))
                return liveData
            }
            firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)
                .collection(NOTES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        liveData.postValue(
                            Resource.Error(
                                error?.message ?: res.getString(R.string.error_unknown)
                            )
                        )
                    } else {
                        val notes = snapshot.documents.mapNotNull { it.toObject(Note::class.java) }
                        liveData.postValue(Resource.Success(notes))
                    }
                }
        } catch (e: Exception) {
            liveData.postValue(Resource.Error(e.message ?: res.getString(R.string.error_unknown)))
        }

        return liveData
    }

    override suspend fun createList(name: String): Resource<Unit> {
        return try {

            val userId = getUserId()
            if (userId == null) {
                Timber.e("UserID is null ??")
                return Resource.Error(res.getString(R.string.error_unknown))
            }
            val newList = NotesList(
                id = firestore.collection(USERS).document(userId).collection(NOTES_LIST)
                    .document().id,
                name = name,
                observers = listOf(),
                date = Timestamp.now()
            )
            firestore.collection(USERS).document(userId).collection(NOTES_LIST)
                .document(newList.id).set(newList).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: res.getString(R.string.error_unknown))
        }
    }

    override suspend fun createNote(listId: String, note: Note): Resource<Unit> {
        return try {

            val userId = getUserId()
            if (userId == null) {
                Timber.e("UserID is null ??")
                return Resource.Error(res.getString(R.string.error_unknown))
            }
            val newNote = note.copy(
                id = firestore.collection(USERS).document(userId)
                    .collection(NOTES_LIST).document(listId)
                    .collection(NOTES).document().id
            )
            firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)
                .collection(NOTES).document(newNote.id).set(newNote).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: res.getString(R.string.error_unknown))
        }
    }
}
