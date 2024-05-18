package com.chemecador.secretaria.data.network.services

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthService @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    suspend fun login(user: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(user, password).await().user
}