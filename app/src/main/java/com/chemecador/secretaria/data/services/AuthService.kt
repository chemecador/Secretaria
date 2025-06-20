package com.chemecador.secretaria.data.services

import android.app.Activity
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.Constants
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val res: ResourceProvider,
) {

    fun getCurrentUserId() = firebaseAuth.currentUser?.uid

    fun getUser() = firebaseAuth.currentUser

    suspend fun getUserCode(): String? {
        val user = firebaseAuth.currentUser ?: return null
        val userId = user.uid
        val userDocRef = firestore.collection(Constants.USERS).document(userId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val userCode = snapshot.getString(Constants.USERCODE)
                if (userCode.isNullOrEmpty()) {
                    runBlocking {
                        val newUserCode =
                            generateUserCode() ?: throw NullPointerException("User code is null")
                        if (!snapshot.exists()) {
                            transaction.set(
                                userDocRef,
                                mapOf(Constants.USERCODE to newUserCode)
                            )
                        } else {
                            transaction.update(userDocRef, Constants.USERCODE, newUserCode)
                        }
                        newUserCode
                    }
                } else {
                    userCode
                }
            }.await()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }


    private suspend fun generateUserCode(): String? {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val yearLastTwoDigits = calendar.get(Calendar.YEAR) % 100
        val dateKey = "$yearLastTwoDigits$dayOfYear"

        val docRef = firestore.collection(Constants.USERCODES).document(dateKey)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val newCounter = if (snapshot.exists()) {
                    val currentCounter = snapshot.getLong(Constants.COUNTER) ?: 0
                    currentCounter + 1
                } else {
                    1
                }
                transaction.set(docRef, mapOf(Constants.COUNTER to newCounter))
                "$yearLastTwoDigits$dayOfYear$newCounter"
            }.await()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun login(user: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(user, password).await()
            Result.success(authResult.user!!)
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e(e)
            Result.failure(Exception(res.getString(R.string.error_email_not_found)))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e)
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
            Timber.e(e)
            Result.failure(Exception(res.getString(R.string.error_email_already_exists)))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Timber.e(e)
            Result.failure(Exception(res.getString(R.string.error_password_invalid)))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e)
            Result.failure(Exception(res.getString(R.string.error_email_invalid)))
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(Exception(e.message))
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(Exception("${res.getString(R.string.error_login)} + ${e.message}"))
        }
    }

    fun loginWithPhone(
        phoneNumber: String,
        activity: Activity,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        // For testing purposes:
        // firebaseAuth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber("+34 123456789", "123456")

        val options = PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(activity).setCallbacks(callback).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyCode(verificationCode: String, phoneCode: String): Result<FirebaseUser?> {
        val credentials = PhoneAuthProvider.getCredential(verificationCode, phoneCode)
        return completeRegisterWithCredential(credentials)
    }

    suspend fun completeRegisterWithPhoneVerification(credentials: PhoneAuthCredential) =
        completeRegisterWithCredential(credentials)

    private suspend fun completeRegisterWithCredential(credential: AuthCredential): Result<FirebaseUser?> {
        return suspendCancellableCoroutine { cancellableContinuation ->
            firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
                cancellableContinuation.resume(Result.success(it.user))
            }.addOnFailureListener {
                cancellableContinuation.resumeWithException(it)
                Timber.e(it)
            }
        }
    }

    suspend fun signInAnonymously(): FirebaseUser? {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            authResult.user
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}
