package com.chemecador.secretaria.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.adapters.FriendRequestAdapter
import com.chemecador.secretaria.api.Client
import com.chemecador.secretaria.api.Service
import com.chemecador.secretaria.databinding.FragmentFriendRequestBinding
import com.chemecador.secretaria.items.Friend
import com.chemecador.secretaria.utils.PreferencesHandler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class FriendRequestFragment : Fragment() {
    private var binding: FragmentFriendRequestBinding? = null
    private var rv: RecyclerView? = null
    private var friendRequestsList: ArrayList<Friend>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendRequestBinding.inflate(
            layoutInflater
        )
        rv = binding?.rvRequests

        friendRequestsList = getFriendRequests()
        binding?.tvNoRequests?.visibility =
            if (friendRequestsList?.isEmpty() == true) View.VISIBLE else View.GONE

        val adapter = FriendRequestAdapter(requireContext(), friendRequestsList)
        rv!!.adapter = adapter
        return binding!!.root
    }

    private fun getFriendRequests(): ArrayList<Friend> {

        var requestsList: ArrayList<Friend> = ArrayList()
        val retrofit: Retrofit? = Client.client
        val apiService: Service? = retrofit?.create(
            Service::class.java
        )
        val call: Call<ArrayList<Friend>>? =
            apiService?.getFriendRequests(PreferencesHandler.getToken(requireContext()), PreferencesHandler.getId(requireContext()))
        call?.enqueue(object : Callback<ArrayList<Friend>> {
            override fun onResponse(
                call: Call<ArrayList<Friend>?>,
                response: Response<ArrayList<Friend>?>
            ) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result: ArrayList<Friend>?  = response.body()

                    requestsList = result!!

                } else if (response.code() == 401) {

                } else {

                }
            }

            override fun onFailure(call: Call<ArrayList<Friend>?>, t: Throwable) {

            }
        })
        return requestsList
    }
}