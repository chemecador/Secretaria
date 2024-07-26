package com.chemecador.secretaria.data.repositories.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.FirestoreConstants.ACCEPTANCE_DATE
import com.chemecador.secretaria.core.constants.FirestoreConstants.FRIENDSHIPS
import com.chemecador.secretaria.core.constants.FirestoreConstants.RECEIVER_ID
import com.chemecador.secretaria.core.constants.FirestoreConstants.SENDER_ID
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val res: ResourceProvider
) : FriendsRepository {

    override fun getFriends(userId: String): LiveData<Resource<List<Friendship>>> {
        val liveData = MutableLiveData<Resource<List<Friendship>>>()
        liveData.postValue(Resource.Loading())

        try {
            firestore.collection(FRIENDSHIPS)
                .whereEqualTo(SENDER_ID, userId)
                .whereNotEqualTo(ACCEPTANCE_DATE, null)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        liveData.postValue(
                            Resource.Error(
                                error?.message ?: res.getString(R.string.error_unknown)
                            )
                        )
                    } else {
                        val friendsList = snapshot.documents.mapNotNull { document ->
                            document.toObject(Friendship::class.java)
                        }.filter { it.acceptanceDate != null }
                        liveData.postValue(Resource.Success(friendsList))
                    }
                }
        } catch (e: Exception) {
            Timber.e(e)
            liveData.postValue(Resource.Error(e.message ?: res.getString(R.string.error_unknown)))
        }
        return liveData
    }

    override fun getFriendRequests(userId: String): LiveData<Resource<List<Friendship>>> {
        val liveData = MutableLiveData<Resource<List<Friendship>>>()
        liveData.postValue(Resource.Loading())

        try {
            firestore.collection(FRIENDSHIPS)
                .whereEqualTo(RECEIVER_ID, userId)
                .whereEqualTo(ACCEPTANCE_DATE, null)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        liveData.postValue(
                            Resource.Error(
                                error?.message ?: res.getString(R.string.error_unknown)
                            )
                        )
                    } else {
                        val requestsList = snapshot.documents.mapNotNull { document ->
                            document.toObject(Friendship::class.java)
                        }.filter { it.acceptanceDate == null }
                        liveData.postValue(Resource.Success(requestsList))
                    }
                }
        } catch (e: Exception) {
            Timber.e(e)
            liveData.postValue(Resource.Error(e.message ?: res.getString(R.string.error_unknown)))
        }
        return liveData
    }
}

