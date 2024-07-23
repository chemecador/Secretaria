package com.chemecador.secretaria.ui.viewmodel.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.repositories.friends.FriendsRepository
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FriendsRepository
) : ViewModel() {

    private val _friends = MutableLiveData<Resource<List<Friendship>>>()
    val friends: LiveData<Resource<List<Friendship>>> = _friends

    private val _friendRequests = MutableLiveData<Resource<List<Friendship>>>()
    val friendRequests: LiveData<Resource<List<Friendship>>> = _friendRequests

    fun loadFriends(userId: String) {
        _friends.postValue(Resource.Loading())
        repository.getFriends(userId).observeForever { resource ->
            _friends.postValue(resource)
        }
    }

    fun loadFriendRequests(userId: String) {
        _friendRequests.postValue(Resource.Loading())
        repository.getFriendRequests(userId).observeForever { resource ->
            _friendRequests.postValue(resource)
        }
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
