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
import com.chemecador.secretaria.databinding.FragmentFriendRequestBinding
import com.chemecador.secretaria.ui.view.rv.adapters.friends.FriendRequestAdapter
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FriendRequestsFragment : Fragment() {

    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var binding: FragmentFriendRequestBinding
    private lateinit var adapter: FriendRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModel()
    }

    private fun initUI() {
        adapter = FriendRequestAdapter(
            onRequestAccepted = { request -> viewModel.acceptFriendRequest(request.id) },
            onRequestRejected = { request -> viewModel.rejectFriendRequest(request.id) }
        )
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter
        viewModel.loadFriendRequests()
    }

    private fun observeViewModel() {
        viewModel.friendRequests.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.tvNoRequests.isVisible = resource.data.isNullOrEmpty()
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.acceptRequestStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    Toast.makeText(
                        context,
                        R.string.label_friend_request_accepted,
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.loadFriendRequests()
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.rejectRequestStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    Toast.makeText(
                        context,
                        R.string.label_friend_request_rejected,
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.loadFriendRequests()
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
