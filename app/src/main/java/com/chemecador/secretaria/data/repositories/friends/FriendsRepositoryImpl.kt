package com.chemecador.secretaria.data.repositories.friends

import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.Constants.ACCEPTANCE_DATE
import com.chemecador.secretaria.core.Constants.FRIENDSHIPS
import com.chemecador.secretaria.core.Constants.RECEIVER_CODE
import com.chemecador.secretaria.core.Constants.RECEIVER_ID
import com.chemecador.secretaria.core.Constants.RECEIVER_NAME
import com.chemecador.secretaria.core.Constants.REQUEST_DATE
import com.chemecador.secretaria.core.Constants.SENDER_ID
import com.chemecador.secretaria.core.Constants.SENDER_NAME
import com.chemecador.secretaria.core.Constants.USERCODE
import com.chemecador.secretaria.core.Constants.USERS
import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val res: ResourceProvider
) : FriendsRepository {

    override fun getFriends(): Flow<Resource<List<Friend>>> = flow {
        emit(Resource.Loading())
        try {
            val userId =
                userRepository.getUserId() ?: throw IllegalArgumentException("User ID is null")

            val senderQuery = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()

            val receiverQuery = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()

            val friends = senderQuery.documents.mapNotNull { document ->
                val friendship = document.toObject(Friendship::class.java)?.copy(id = document.id)
                friendship?.let {
                    val friendName = it.receiverName
                    val friendId = it.receiverId
                    Friend(id = friendId, name = friendName)
                }
            } + receiverQuery.documents.mapNotNull { document ->
                val friendship = document.toObject(Friendship::class.java)?.copy(id = document.id)
                friendship?.let {
                    val friendName = it.senderName
                    val friendId = it.senderId
                    Friend(id = friendId, name = friendName)
                }
            }

            emit(Resource.Success(friends.distinctBy { it.id }))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(res.getString(R.string.error_fetching_friends)))
        }
    }


    override fun getFriendships(): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val userId =
                userRepository.getUserId() ?: throw IllegalArgumentException("User ID is null")

            val senderQuery = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()

            val receiverQuery = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .get()
                .await()

            val friends = senderQuery.documents.mapNotNull { document ->
                document.toObject(Friendship::class.java)?.copy(id = document.id)
            } + receiverQuery.documents.mapNotNull { document ->
                document.toObject(Friendship::class.java)?.copy(id = document.id)
            }

            emit(Resource.Success(friends.distinctBy { it.id }))
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

    override fun getFriendRequests(): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = userRepository.getUserId()
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
            Timber.e(e)
            emit(Resource.Error(res.getString(R.string.error_fetching_friend_requests)))
        }
    }

    override fun getFriendRequestsSent(): Flow<Resource<List<Friendship>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = userRepository.getUserId()
            val snapshot = firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, userId)
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

            val receiverName = userRepository.getUsername()

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
            val userId = userRepository.getUserId()

            val docRef = firestore.collection(FRIENDSHIPS).document(requestId)
            val document = docRef.get().await()
            val receiverId = document.getString(RECEIVER_ID)

            if (userId != receiverId)
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
            val userId = userRepository.getUserId()

            val docRef = firestore.collection(FRIENDSHIPS).document(requestId)
            val document = docRef.get().await()
            val senderId = document.getString(SENDER_ID)

            if (userId != senderId)
                return Resource.Error(res.getString(R.string.error_rejecting_friend_request))

            docRef.delete().await()
            Resource.Success(null)

        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_rejecting_friend_request))
        }
    }

    override suspend fun checkIfAlreadyFriends(friendId: String): Boolean {
        val userId = userRepository.getUserId()
        val snapshot = firestore.collection(FRIENDSHIPS)
            .whereIn(SENDER_ID, listOf(userId, friendId))
            .whereIn(RECEIVER_ID, listOf(userId, friendId))
            .whereNotEqualTo(ACCEPTANCE_DATE, null)
            .get()
            .await()
        return !snapshot.isEmpty
    }

    override suspend fun getUserIdByUserCode(friendCode: String): String? {
        val snapshot = firestore.collection(USERS)
            .whereEqualTo(USERCODE, friendCode)
            .get()
            .await()
        return if (snapshot.isEmpty) null else snapshot.documents.first().id
    }


    override suspend fun sendFriendRequest(friendCode: String): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
            val senderName = userRepository.getUsername()

            val friendQuerySnapshot = firestore.collection(USERS)
                .whereEqualTo(USERCODE, friendCode)
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
}
