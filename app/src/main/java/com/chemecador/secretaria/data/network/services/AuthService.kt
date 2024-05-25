package com.chemecador.secretaria.data.network.services

import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val res: ResourceProvider,
) {
    suspend fun login(user: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(user, password).await()
            Result.success(authResult.user!!)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception(res.getString(R.string.error_email_not_found)))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception(res.getString(R.string.error_password_wrong)))
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(Exception(e.message))
        }
    }

    suspend fun signup(user: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(user, password).await()
            Result.success(authResult.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception(res.getString(R.string.error_email_already_exists)))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception(res.getString(R.string.error_password_invalid)))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception(res.getString(R.string.error_email_invalid)))
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(Exception(e.message))
        }
    }
}
