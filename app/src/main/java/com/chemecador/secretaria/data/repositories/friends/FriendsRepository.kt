package com.chemecador.secretaria.data.repositories.friends

import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    fun getFriends(): Flow<Resource<List<Friend>>>
    fun getFriendships(): Flow<Resource<List<Friendship>>>
    suspend fun deleteFriend(friendshipId: String): Resource<Unit>
    fun getPendingFriendRequests(): Flow<Resource<List<Friendship>>>
    suspend fun getUserIdByUserCode(friendCode: String): String?
    suspend fun checkIfAlreadyFriends(friendId: String): Boolean
    suspend fun sendFriendRequest(friendCode: String): Resource<Unit>
    suspend fun acceptFriendRequest(requestId: String): Resource<Unit>
    suspend fun rejectFriendRequest(requestId: String): Resource<Unit>
    suspend fun getFriendRequestsSent(): Flow<List<Friendship>>
    suspend fun cancelFriendRequest(requestId: String): Resource<Unit>
}
