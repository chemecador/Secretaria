package com.chemecador.secretaria.data.repositories.main

import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.Constants.CONTRIBUTORS
import com.chemecador.secretaria.core.constants.Constants.DATE
import com.chemecador.secretaria.core.constants.Constants.NOTES
import com.chemecador.secretaria.core.constants.Constants.NOTES_LIST
import com.chemecador.secretaria.core.constants.Constants.USERS
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val res: ResourceProvider
) : MainRepository {

    private suspend fun getUserId(): String? {
        return userRepository.userId.firstOrNull()
    }

    override suspend fun getLists(): Resource<List<NotesList>> {
        return try {
            val userId = getUserId() ?: throw IllegalArgumentException("User ID is null")
            val snapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()
            val lists = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(NotesList::class.java)?.copy(id = documentSnapshot.id)
            }
            Resource.Success(lists)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_fetching_lists))
        }
    }



    override suspend fun getNotes(listId: String): Resource<List<Note>> {
        return try {
            val userId = getUserId()
            if (userId == null) {
                Timber.e("UserID is null ??")
                return Resource.Error(res.getString(R.string.error_unknown))
            }
            val snapshot = firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)
                .collection(NOTES)
                .orderBy(DATE, Query.Direction.DESCENDING)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Note::class.java)?.copy(id = documentSnapshot.id)
            }
            Resource.Success(notes)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
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
                contributors = listOf(userId),
                date = Timestamp.now()
            )
            firestore.collection(USERS).document(userId).collection(NOTES_LIST)
                .document(newList.id).set(newList).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun createNote(listId: String, note: Note): Resource<Unit> {
        return try {

            val userId = getUserId()
            if (userId == null) {
                Timber.e("UserID is null ??")
                return Resource.Error(res.getString(R.string.error_user_not_auth))
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
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun getNote(listId: String, noteId: String): Resource<Note> {
        return try {
            val userId =
                getUserId() ?: return Resource.Error(res.getString(R.string.error_invalid_userid))

            val snapshot = firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)
                .collection(NOTES).document(noteId)
                .get().await()

            val note = snapshot.toObject(Note::class.java)
            if (note != null) {
                Resource.Success(note)
            } else {
                Resource.Error(res.getString(R.string.error_note_not_found))
            }
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }


    override suspend fun deleteNote(listId: String, noteId: String): Resource<Unit> {
        return try {
            val userId =
                getUserId() ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)
                .collection(NOTES).document(noteId).delete().await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_deleting_note))
        }
    }


    override suspend fun editNote(listId: String, note: Note): Resource<Unit> {
        return try {
            val userId =
                getUserId() ?: return Resource.Error(res.getString(R.string.error_invalid_userid))

            firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)
                .collection(NOTES).document(note.id)
                .set(note).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }


    override suspend fun deleteList(listId: String): Resource<Unit> {
        return try {
            val userId = getUserId()
                ?: throw IllegalStateException(res.getString(R.string.error_invalid_userid))

            val listRef =
                firestore.collection(USERS).document(userId).collection(NOTES_LIST).document(listId)
            val notesRef = listRef.collection(NOTES)

            val taskResult = notesRef.get().await()

            val batch = firestore.batch()
            for (document in taskResult.documents) {
                batch.delete(document.reference)
            }

            batch.commit().await()

            listRef.delete().await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }


    override suspend fun editList(updatedList: NotesList): Resource<Unit> {
        return try {
            val userId = getUserId() ?: return Resource.Error("UserID is null ??")

            firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(updatedList.id)
                .set(updatedList)
                .await()

            Resource.Success(null)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_updating_list))
        }
    }


    @Suppress("UNCHECKED_CAST")
    override suspend fun addContributorToList(listId: String, friendId: String): Resource<Unit> {
        return try {
            val userId = getUserId() ?: return Resource.Error("User ID is null")

            val listRef = firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(listRef)
                val contributors =
                    snapshot.get(CONTRIBUTORS) as? MutableList<String> ?: mutableListOf()
                if (!contributors.contains(friendId)) {
                    contributors.add(friendId)
                    transaction.update(listRef, CONTRIBUTORS, contributors)
                }
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_adding_contributor))
        }
    }
}
