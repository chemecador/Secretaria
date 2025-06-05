package com.chemecador.secretaria.ui.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.services.AuthService
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    fun checkRoute() =
        if (authService.getUser() == null) {
            Route.Login
        } else {
            Route.Main
        }

    fun saveToken(token: String) {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUserId()
                if (userId != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userTokensRef = db.collection("users")
                        .document(userId)
                        .collection("fcm_tokens")

                    userTokensRef.get().addOnSuccessListener { snapshot ->
                        val batch = db.batch()
                        snapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }

                        val tokenData = hashMapOf(
                            "token" to token,
                            "timestamp" to FieldValue.serverTimestamp()
                        )
                        batch.set(userTokensRef.document(token), tokenData)

                        batch.commit().addOnSuccessListener {
                            Timber.tag("FCM").d("Token successfully saved")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag("FCM").e(e, "Error saving token")
            }
        }
    }
}

sealed class Route {
    data object Login : Route()
    data object Main : Route()
}