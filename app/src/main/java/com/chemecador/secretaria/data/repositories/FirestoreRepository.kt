package com.chemecador.secretaria.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val res: ResourceProvider
) : OnlineRepository {

    private val usersRef = firestore.collection(USERS)

    override fun getAllLists(userId: String): LiveData<Resource<List<NotesList>>> {
        val liveData = MutableLiveData<Resource<List<NotesList>>>()
        liveData.postValue(Resource.Loading())

        usersRef.document(userId).collection(NOTES_LIST)
            .addSnapshotListener { snapshot, error ->
                try {
                    if (error != null) {
                        val msg = error.message ?: res.getString(R.string.error_unknown)
                        liveData.postValue(Resource.Error(msg))
                        Timber.e(msg)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val lists = snapshot.documents.mapNotNull { it.toObject<NotesList>() }
                        Timber.i("Snapshots: $lists")
                        liveData.postValue(Resource.Success(lists))
                    } else {
                        Timber.e("Snapshot is null")
                        liveData.postValue(Resource.Error(res.getString(R.string.label_empty_data)))
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    liveData.postValue(Resource.Error(e.message.toString()))
                }
            }
        return liveData
    }


    override suspend fun createList(userId: String, name: String) {
        val newList = NotesList(
            id = firestore.collection(USERS).document(userId).collection(NOTES_LIST).document().id,
            name = name,
            observers = listOf(),
            date = com.google.firebase.Timestamp.now()
        )
        firestore.collection(USERS).document(userId).collection(NOTES_LIST)
            .document(newList.id).set(newList).await()
    }

    companion object {
        private const val USERS = "users"
        private const val NOTES_LIST = "noteslist"
        private const val NOTES = "notes"
    }
}