package com.chemecador.secretaria.ui.view.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentFriendListBinding
import com.chemecador.secretaria.ui.view.rv.adapters.friends.FriendListAdapter
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendListFragment : Fragment() {

    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var binding: FragmentFriendListBinding
    private lateinit var adapter: FriendListAdapter
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = viewModel.getCurrentUserId() ?: return

        initUI()
        observeViewModel()
    }

    private fun initUI() {

        adapter = FriendListAdapter(
            currentUserId = userId,
            onDeleteFriend = { friend ->
                viewModel.deleteFriend(friend.id)
            }
        )
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter

    }

    private fun observeViewModel() {
        viewModel.loadFriendships()
        viewModel.friendships.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.pb.visibility = View.GONE
                    binding.tvNoFriends.isVisible = resource.data.isNullOrEmpty()
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.deleteFriendStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.pb.visibility = View.GONE
                    Toast.makeText(
                        context,
                        getString(R.string.label_friend_deleted),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.loadFriendships()
                }

                is Resource.Error -> {
                    binding.pb.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
