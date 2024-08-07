package com.chemecador.secretaria.ui.viewmodel.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.friends.FriendsRepository
import com.chemecador.secretaria.data.services.AuthService
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FriendsRepository,
    private val userPreferences: UserPreferences,
    private val authService: AuthService,
    private val res: ResourceProvider
) : ViewModel() {

    private val _friendships = MutableLiveData<Resource<List<Friendship>>>()
    val friendships: LiveData<Resource<List<Friendship>>> = _friendships

    private val _friends = MutableLiveData<Resource<List<Friend>>>()
    val friends: LiveData<Resource<List<Friend>>> = _friends

    private val _deleteFriendStatus = MutableLiveData<Resource<Unit>>()
    val deleteFriendStatus: LiveData<Resource<Unit>> get() = _deleteFriendStatus

    private val _friendRequests = MutableLiveData<Resource<List<Friendship>>>()
    val friendRequests: LiveData<Resource<List<Friendship>>> = _friendRequests

    private val _acceptRequestStatus = MutableLiveData<Resource<Unit>>()
    val acceptRequestStatus: LiveData<Resource<Unit>> = _acceptRequestStatus

    private val _rejectRequestStatus = MutableLiveData<Resource<Unit>>()
    val rejectRequestStatus: LiveData<Resource<Unit>> = _rejectRequestStatus

    private val _addFriendStatus = MutableLiveData<Resource<Unit>>()
    val addFriendStatus: LiveData<Resource<Unit>> = _addFriendStatus

    private val _userCode = MutableLiveData<String?>()
    val userCode: LiveData<String?> get() = _userCode

    private val _friendRequestsSent = MutableLiveData<Resource<List<Friendship>>>()
    val friendRequestSent: LiveData<Resource<List<Friendship>>> = _friendRequestsSent

    private val _cancelRequestStatus = MutableLiveData<Resource<Unit>>()
    val cancelRequestStatus: LiveData<Resource<Unit>> = _cancelRequestStatus

    fun getCurrentUserId() = FirebaseAuth.getInstance().currentUser?.uid

    fun loadFriendships() {
        viewModelScope.launch {
            repository.getFriendships().collect { resource ->
                _friendships.postValue(resource)
            }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            repository.getFriends().collect { resource ->
                _friends.postValue(resource)
            }
        }
    }


    fun deleteFriend(friendshipId: String) {
        viewModelScope.launch {
            _deleteFriendStatus.value = repository.deleteFriend(friendshipId)
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            repository.getFriendRequests().collect { resource ->
                _friendRequests.postValue(resource)
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.acceptFriendRequest(requestId)
            _acceptRequestStatus.postValue(result)
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.rejectFriendRequest(requestId)
            _rejectRequestStatus.postValue(result)
        }
    }

    fun sendFriendRequest(friendCode: String) {
        viewModelScope.launch {
            _addFriendStatus.value = Resource.Loading()
            val friendId = repository.getUserIdByUserCode(friendCode) ?: run {
                _addFriendStatus.value =
                    Resource.Error(res.getString(R.string.error_user_not_found))
                return@launch
            }

            val alreadyFriends = repository.checkIfAlreadyFriends(friendId)
            if (alreadyFriends) {
                _addFriendStatus.value =
                    Resource.Error(res.getString(R.string.error_already_friends))
                return@launch
            }

            val result = repository.sendFriendRequest(friendId)
            _addFriendStatus.value = result
        }
    }

    fun loadUserCode() {
        viewModelScope.launch {
            val userCode = userPreferences.userCodeFlow.firstOrNull()
            if (userCode != null) {
                _userCode.postValue(userCode)
            } else {
                val newUserCode = authService.getUserCode()
                if (newUserCode != null) {
                    userPreferences.saveUserCode(newUserCode)
                    _userCode.postValue(newUserCode)
                } else {
                    _userCode.postValue(null)
                }
            }
        }
    }

    fun loadFriendRequestsSent() {
        viewModelScope.launch {
            _friendRequestsSent.value = Resource.Loading()
            try {
                repository.getFriendRequestsSent().collect {
                    _friendRequestsSent.postValue(it)
                }
            } catch (e: Exception) {
                _friendRequestsSent.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun cancelFriendRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.cancelFriendRequest(requestId)
            _cancelRequestStatus.postValue(result)
        }
    }
}

