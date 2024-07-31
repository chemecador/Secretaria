package com.chemecador.secretaria.data.repositories.friends

import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    fun getFriends(userId: String): Flow<Resource<List<Friendship>>>
    suspend fun deleteFriend(friendshipId: String): Resource<Void>
    fun getPendingFriendRequests(userId: String): Flow<Resource<List<Friendship>>>
    suspend fun sendFriendRequest(friendCode: String): Resource<Void>
    suspend fun acceptFriendRequest(requestId: String): Resource<Void>
    suspend fun rejectFriendRequest(requestId: String): Resource<Void>
}
