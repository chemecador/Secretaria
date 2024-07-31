package com.chemecador.secretaria.ui.viewmodel.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.model.Friendship
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
    private val authService: AuthService
) : ViewModel() {

    private val _friends = MutableLiveData<Resource<List<Friendship>>>()
    val friends: LiveData<Resource<List<Friendship>>> = _friends

    private val _deleteFriendStatus = MutableLiveData<Resource<Void>>()
    val deleteFriendStatus: LiveData<Resource<Void>> get() = _deleteFriendStatus

    private val _friendRequests = MutableLiveData<Resource<List<Friendship>>>()
    val friendRequests: LiveData<Resource<List<Friendship>>> = _friendRequests

    private val _acceptRequestStatus = MutableLiveData<Resource<Void>>()
    val acceptRequestStatus: LiveData<Resource<Void>> = _acceptRequestStatus

    private val _rejectRequestStatus = MutableLiveData<Resource<Void>>()
    val rejectRequestStatus: LiveData<Resource<Void>> = _rejectRequestStatus


    private val _addFriendStatus = MutableLiveData<Resource<Void>>()
    val addFriendStatus: LiveData<Resource<Void>> = _addFriendStatus

    private val _userCode = MutableLiveData<String?>()
    val userCode: LiveData<String?> get() = _userCode

    fun getCurrentUserId() = FirebaseAuth.getInstance().currentUser?.uid

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            repository.getFriends(userId).collect { resource ->
                _friends.postValue(resource)
            }
        }
    }


    fun deleteFriend(friendshipId: String) {
        viewModelScope.launch {
            _deleteFriendStatus.value = repository.deleteFriend(friendshipId)
        }
    }

    fun loadFriendRequests(userId: String) {
        viewModelScope.launch {
            repository.getPendingFriendRequests(userId).collect { resource ->
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
            val result = repository.sendFriendRequest(friendCode)
            _addFriendStatus.postValue(result)
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
}

