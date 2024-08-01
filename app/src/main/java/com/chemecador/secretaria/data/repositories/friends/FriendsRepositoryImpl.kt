package com.chemecador.secretaria.data.repositories.friends

import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.FirestoreConstants
import com.chemecador.secretaria.core.constants.FirestoreConstants.ACCEPTANCE_DATE
import com.chemecador.secretaria.core.constants.FirestoreConstants.FRIENDSHIPS
import com.chemecador.secretaria.core.constants.FirestoreConstants.RECEIVER_CODE
import com.chemecador.secretaria.core.constants.FirestoreConstants.RECEIVER_ID
import com.chemecador.secretaria.core.constants.FirestoreConstants.RECEIVER_NAME
import com.chemecador.secretaria.core.constants.FirestoreConstants.REQUEST_DATE
import com.chemecador.secretaria.core.constants.FirestoreConstants.SENDER_ID
import com.chemecador.secretaria.core.constants.FirestoreConstants.SENDER_NAME
import com.chemecador.secretaria.core.constants.FirestoreConstants.USERS
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.services.AuthService
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    override fun getFriends(userId: String): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()
            val friends = snapshot.documents.mapNotNull { document ->
                document.toObject(Friendship::class.java)?.copy(id = document.id)
            }
            emit(Resource.Success(friends))
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    e.localizedMessage ?: res.getString(R.string.error_fetching_friends)
                )
            )
        }
    }


    override suspend fun deleteFriend(friendshipId: String): Resource<Void> {
        return try {
            firestore.collection(FRIENDSHIPS).document(friendshipId).delete().await()
            Resource.Success(null)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_deleting_friend))
        }
    }

    override fun getPendingFriendRequests(userId: String): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, userId)
                .whereEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()
            val friendRequests = snapshot.documents.mapNotNull { document ->
                document.toObject(Friendship::class.java)?.copy(id = document.id)
            }
            emit(Resource.Success(friendRequests))
        } catch (e: Exception) {
            emit(Resource.Error(res.getString(R.string.error_fetching_friend_requests)))
        }
    }


    override suspend fun acceptFriendRequest(requestId: String): Resource<Void> {
        return try {
            val user = authService.getUser()
                ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))
            val receiverName = user.email ?: user.displayName ?: user.phoneNumber
            ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_found))

            val updates = mapOf(
                ACCEPTANCE_DATE to FieldValue.serverTimestamp(),
                RECEIVER_NAME to receiverName
            )

            firestore.collection(FRIENDSHIPS).document(requestId)
                .update(updates)
                .await()

            Resource.Success(null)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_accepting_friend_request))
        }
    }

    override suspend fun rejectFriendRequest(requestId: String): Resource<Void> {
        return try {
            val currentUser =
                authService.getUser()
                    ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val docRef = firestore.collection(FRIENDSHIPS).document(requestId)
            val document = docRef.get().await()
            val receiverId = document.getString(RECEIVER_ID)

            if (currentUser.uid != receiverId)
                return Resource.Error(res.getString(R.string.error_rejecting_friend_request))

            docRef.delete().await()
            Resource.Success(null)

        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_rejecting_friend_request))
        }
    }

    override suspend fun cancelFriendRequest(requestId: String): Resource<Void> {
        return try {
            val currentUser =
                authService.getUser()
                    ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val docRef = firestore.collection(FRIENDSHIPS).document(requestId)
            val document = docRef.get().await()
            val senderId = document.getString(SENDER_ID)

            if (currentUser.uid != senderId)
                return Resource.Error(res.getString(R.string.error_rejecting_friend_request))

            docRef.delete().await()
            Resource.Success(null)

        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_rejecting_friend_request))
        }
    }


    override suspend fun sendFriendRequest(friendCode: String): Resource<Void> {
        return try {
            val user =
                authService.getUser()
                    ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))
            val userId = user.uid

            val senderName = user.email ?: user.displayName ?: user.phoneNumber
            ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_found))

            val friendQuerySnapshot = firestore.collection(USERS)
                .whereEqualTo(FirestoreConstants.USERCODE, friendCode)
                .get()
                .await()

            if (friendQuerySnapshot.documents.isEmpty()) {
                throw IllegalArgumentException(res.getString(R.string.error_user_not_found))
            }
            val friendDoc = friendQuerySnapshot.documents.first()
            val friendId = friendDoc.id

            val friendship = hashMapOf(
                SENDER_ID to userId,
                SENDER_NAME to senderName,
                RECEIVER_ID to friendId,
                RECEIVER_CODE to friendCode,
                REQUEST_DATE to FieldValue.serverTimestamp(),
                ACCEPTANCE_DATE to null
            )

            firestore.collection(FRIENDSHIPS)
                .add(friendship)
                .await()

            Resource.Success(null)

        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            Resource.Error(e.localizedMessage ?: res.getString(R.string.error_sending_request))
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_sending_request))
        }
    }

    override suspend fun getFriendRequestsSent(userId: String): Flow<List<Friendship>> {
        return callbackFlow {
            val subscription = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, userId)
                .whereEqualTo(ACCEPTANCE_DATE, null)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val requests = snapshot?.documents?.mapNotNull { document ->
                        document.toObject(Friendship::class.java)?.copy(id = document.id)
                    }.orEmpty()
                    trySend(requests).isSuccess
                }
            awaitClose { subscription.remove() }
        }.flowOn(Dispatchers.IO)
    }
}
