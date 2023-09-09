package com.chemecador.secretaria.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.adapters.FriendListAdapter
import com.chemecador.secretaria.databinding.FragmentFriendListBinding
import com.chemecador.secretaria.items.Friend

class FriendListFragment : Fragment() {
    private var binding: FragmentFriendListBinding? = null
    private var rv: RecyclerView? = null
    private var friendList: MutableList<Friend>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendListBinding.inflate(
            layoutInflater
        )
        rv = binding!!.rvFriends

        if (friendList?.isEmpty() == true) {
            binding!!.tvNoFriends.visibility = View.VISIBLE
        } else {
            binding!!.tvNoFriends.visibility = View.GONE
        }
        val adapter = FriendListAdapter(requireContext(), friendList)
        rv!!.adapter = adapter
        return binding!!.root
    }
}