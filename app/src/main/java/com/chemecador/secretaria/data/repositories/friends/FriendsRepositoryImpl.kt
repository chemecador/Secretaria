package com.chemecador.secretaria.data.repositories.friends

import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.Constants
import com.chemecador.secretaria.core.constants.Constants.ACCEPTANCE_DATE
import com.chemecador.secretaria.core.constants.Constants.FRIENDSHIPS
import com.chemecador.secretaria.core.constants.Constants.RECEIVER_CODE
import com.chemecador.secretaria.core.constants.Constants.RECEIVER_ID
import com.chemecador.secretaria.core.constants.Constants.RECEIVER_NAME
import com.chemecador.secretaria.core.constants.Constants.REQUEST_DATE
import com.chemecador.secretaria.core.constants.Constants.SENDER_ID
import com.chemecador.secretaria.core.constants.Constants.SENDER_NAME
import com.chemecador.secretaria.core.constants.Constants.USERS
import com.chemecador.secretaria.data.model.Friend
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

    override fun getFriends(): Flow<Resource<List<Friend>>> = flow {
        emit(Resource.Loading())
        try {
            val user =
                authService.getUser()
                    ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))
            val snapshot = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, user.uid)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()
            val friends = snapshot.documents.mapNotNull { document ->
                val friendship = document.toObject(Friendship::class.java)?.copy(id = document.id)
                friendship?.let {
                    val friendName = if (user.uid == it.senderId) it.receiverName else it.senderName
                    val friendId = if (user.uid == it.senderId) it.receiverId else it.senderId
                    Friend(id = friendId, name = friendName)
                }
            }
            emit(Resource.Success(friends))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(res.getString(R.string.error_fetching_friends)))
        }
    }

    override fun getFriendships(): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val user =
                authService.getUser()
                    ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))
            val snapshot = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, user.uid)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()
            val friends = snapshot.documents.mapNotNull { document ->
                document.toObject(Friendship::class.java)?.copy(id = document.id)
            }
            emit(Resource.Success(friends))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(res.getString(R.string.error_fetching_friends)))
        }
    }


    override suspend fun deleteFriend(friendshipId: String): Resource<Unit> {
        return try {
            firestore.collection(FRIENDSHIPS).document(friendshipId).delete().await()
            Resource.Success(null)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_deleting_friend))
        }
    }

    override fun getPendingFriendRequests(): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val user =
                authService.getUser()
                    ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))
            val snapshot = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, user.uid)
                .whereEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()
            val friendRequests = snapshot.documents.mapNotNull { document ->
                document.toObject(Friendship::class.java)?.copy(id = document.id)
            }
            emit(Resource.Success(friendRequests))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(res.getString(R.string.error_fetching_friend_requests)))
        }
    }


    override suspend fun acceptFriendRequest(requestId: String): Resource<Unit> {
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

    override suspend fun rejectFriendRequest(requestId: String): Resource<Unit> {
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

    override suspend fun cancelFriendRequest(requestId: String): Resource<Unit> {
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


    override suspend fun sendFriendRequest(friendCode: String): Resource<Unit> {
        return try {
            val user =
                authService.getUser()
                    ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))

            val senderName = user.email ?: user.displayName ?: user.phoneNumber
            ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_found))

            val friendQuerySnapshot = firestore.collection(USERS)
                .whereEqualTo(Constants.USERCODE, friendCode)
                .get()
                .await()

            if (friendQuerySnapshot.documents.isEmpty()) {
                throw IllegalArgumentException(res.getString(R.string.error_user_not_found))
            }
            val friendDoc = friendQuerySnapshot.documents.first()
            val friendId = friendDoc.id

            val friendship = hashMapOf(
                SENDER_ID to user.uid,
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

    override suspend fun getFriendRequestsSent(): Flow<List<Friendship>> {

        val user =
            authService.getUser()
                ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))

        return callbackFlow {
            val subscription = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, user.uid)
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
