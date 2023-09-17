package com.chemecador.secretaria.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.adapters.FriendListAdapter
import com.chemecador.secretaria.network.retrofit.Client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.databinding.FragmentFriendListBinding
import com.chemecador.secretaria.items.Friend
import com.chemecador.secretaria.utils.PreferencesHandler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class FriendListFragment : Fragment() {
    private var binding: FragmentFriendListBinding? = null
    private var rv: RecyclerView? = null
    private var friendList: ArrayList<Friend> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendListBinding.inflate(
            layoutInflater
        )
        rv = binding!!.rvFriends

        getFriends()

        if (friendList.isEmpty()) {
            binding!!.tvNoFriends.visibility = View.VISIBLE
        } else {
            binding!!.tvNoFriends.visibility = View.GONE
        }
        val adapter = FriendListAdapter(requireContext(), friendList)
        rv!!.adapter = adapter
        return binding!!.root
    }

    private fun getFriends() {

        val retrofit: Retrofit? = Client.client
        val apiService: Service? = retrofit?.create(
            Service::class.java
        )
        val call: Call<ArrayList<Friend>> = apiService?.getFriends(PreferencesHandler.getToken(requireContext()), PreferencesHandler.getId(requireContext()))!!
        call.enqueue(object : Callback<ArrayList<Friend>> {
            override fun onResponse(
                call: Call<ArrayList<Friend>?>,
                response: Response<ArrayList<Friend>?>
            ) {
                if (response.isSuccessful) {

                    // Procesar la respuesta exitosa
                    val result: ArrayList<Friend>?  = response.body()

                    if (result != null) {
                        friendList = result
                    }

                } else if (response.code() == 401) {

                } else {

                }
            }

            override fun onFailure(call: Call<ArrayList<Friend>?>, t: Throwable) {

            }
        })

    }
}