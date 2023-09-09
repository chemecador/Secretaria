package com.chemecador.secretaria.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.adapters.FriendRequestAdapter
import com.chemecador.secretaria.databinding.FragmentFriendRequestBinding
import com.chemecador.secretaria.items.Friend

class FriendRequestFragment : Fragment() {
    private var binding: FragmentFriendRequestBinding? = null
    private var rv: RecyclerView? = null
    private var friendList: MutableList<Friend>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendRequestBinding.inflate(
            layoutInflater
        )
        rv = binding!!.rvRequests
        if (friendList?.isEmpty() == true) {
            binding!!.tvNoRequests.visibility = View.VISIBLE
        } else {
            binding!!.tvNoRequests.visibility = View.GONE
        }
        val adapter = FriendRequestAdapter(requireContext(), friendList)
        rv!!.adapter = adapter
        return binding!!.root
    }
}