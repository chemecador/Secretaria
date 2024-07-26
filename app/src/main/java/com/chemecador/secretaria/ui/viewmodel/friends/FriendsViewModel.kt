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

    private val _userCode = MutableLiveData<String?>()
    val userCode: LiveData<String?> get() = _userCode

    fun loadFriends(userId: String) {
        _friends.postValue(Resource.Loading())
        repository.getFriends(userId).observeForever { resource ->
            _friends.postValue(resource)
        }
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
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
