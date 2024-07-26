package com.chemecador.secretaria.data.repositories.friends

import androidx.lifecycle.LiveData
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.model.User
import com.chemecador.secretaria.utils.Resource

interface FriendsRepository {
    fun getFriends(userId: String): LiveData<Resource<List<Friendship>>>
    fun getFriendRequests(userId: String): LiveData<Resource<List<Friendship>>>
}
