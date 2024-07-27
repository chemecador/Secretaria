package com.chemecador.secretaria.data.repositories.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.FirestoreConstants
import com.chemecador.secretaria.core.constants.FirestoreConstants.ACCEPTANCE_DATE
import com.chemecador.secretaria.core.constants.FirestoreConstants.FRIENDSHIPS
import com.chemecador.secretaria.core.constants.FirestoreConstants.RECEIVER_ID
import com.chemecador.secretaria.core.constants.FirestoreConstants.RECEIVER_NAME
import com.chemecador.secretaria.core.constants.FirestoreConstants.REQUEST_DATE
import com.chemecador.secretaria.core.constants.FirestoreConstants.SENDER_ID
import com.chemecador.secretaria.core.constants.FirestoreConstants.SENDER_NAME
import com.chemecador.secretaria.core.constants.FirestoreConstants.USERNAME
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.services.AuthService
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AuthService,
    private val res: ResourceProvider
) : FriendsRepository {

    override fun getFriends(userId: String): LiveData<Resource<List<Friendship>>> {
        val liveData = MutableLiveData<Resource<List<Friendship>>>()
        liveData.postValue(Resource.Loading())

        try {
            firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        liveData.postValue(
                            Resource.Error(
                                error?.message ?: res.getString(R.string.error_unknown)
                            )
                        )
                    } else {
                        val friendsList = snapshot.documents.mapNotNull { document ->
                            document.toObject(Friendship::class.java)
                        }.filter { it.acceptanceDate != null }
                        liveData.postValue(Resource.Success(friendsList))
                    }
                }
        } catch (e: Exception) {
            Timber.e(e)
            liveData.postValue(Resource.Error(e.message ?: res.getString(R.string.error_unknown)))
        }
        return liveData
    }

    override fun getFriendRequests(userId: String): LiveData<Resource<List<Friendship>>> {
        val liveData = MutableLiveData<Resource<List<Friendship>>>()
        liveData.postValue(Resource.Loading())

        try {
            firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, userId)
                .whereEqualTo(ACCEPTANCE_DATE, null)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        liveData.postValue(
                            Resource.Error(
                                error?.message ?: res.getString(R.string.error_unknown)
                            )
                        )
                    } else {
                        val requestsList = snapshot.documents.mapNotNull { document ->
                            document.toObject(Friendship::class.java)
                        }.filter { it.acceptanceDate == null }
                        liveData.postValue(Resource.Success(requestsList))
                    }
                }
        } catch (e: Exception) {
            Timber.e(e)
            liveData.postValue(Resource.Error(e.message ?: res.getString(R.string.error_unknown)))
        }
        return liveData
    }

    override suspend fun sendFriendRequest(friendCode: String): Resource<Void> {
        return try {
            val user = authService.getUser()
                ?: throw Exception(res.getString(R.string.error_session_expired))
            val userId = user.uid
            val senderName = user.email ?: res.getString(R.string.label_undefined)

            val friendQuerySnapshot = firestore.collection(FirestoreConstants.USERS)
                .whereEqualTo(FirestoreConstants.USERCODE, friendCode)
                .get()
                .await()

            if (friendQuerySnapshot.documents.isEmpty()) {
                Resource.Error(res.getString(R.string.error_usercode_not_found))
            } else {
                val friendDoc = friendQuerySnapshot.documents.first()
                val friendId = friendDoc.id
                val receiverName =
                    friendDoc.getString(USERNAME) ?: res.getString(R.string.label_undefined)

                val friendship = hashMapOf(
                    SENDER_ID to userId,
                    RECEIVER_ID to friendId,
                    SENDER_NAME to senderName,
                    RECEIVER_NAME to receiverName,
                    REQUEST_DATE to FieldValue.serverTimestamp(),
                    ACCEPTANCE_DATE to null
                )

                firestore.collection(FRIENDSHIPS)
                    .add(friendship)
                    .await()

                Resource.Success(null)
            }
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_sending_request))
        }
    }

}

