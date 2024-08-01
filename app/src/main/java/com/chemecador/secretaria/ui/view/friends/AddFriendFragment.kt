package com.chemecador.secretaria.ui.view.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.FragmentAddFriendBinding
import com.chemecador.secretaria.ui.view.rv.adapters.friends.FriendRequestSentAdapter
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.utils.DeviceUtils
import com.chemecador.secretaria.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFriendFragment : Fragment() {

    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var adapter: FriendRequestSentAdapter
    private lateinit var userId: String
    private var sentFriendRequests: List<Friendship> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = viewModel.getCurrentUserId() ?: return
        observeViewModel()
        setupUI()
    }

    private fun observeViewModel() {
        viewModel.userCode.observe(viewLifecycleOwner) { userCode ->
            if (userCode != null) {
                binding.tvYourCode.text = userCode
            }
        }

        viewModel.addFriendStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    binding.pb.isVisible = false
                    Snackbar.make(
                        binding.root,
                        R.string.label_friend_request_sent,
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(R.id.bnv_friends)
                        .show()
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }
            }
        }

        viewModel.friendRequestSent.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.tvEmpty.isVisible = resource.data.isNullOrEmpty()
                    sentFriendRequests = resource.data ?: listOf()
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.cancelRequestStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    Toast.makeText(
                        context,
                        R.string.label_friend_request_cancelled,
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.loadFriendRequestsSent(userId)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupUI() {
        adapter = FriendRequestSentAdapter { request ->
            viewModel.cancelFriendRequest(request.id)
        }

        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.rv.adapter = adapter
        binding.btnSendRequest.setOnClickListener {
            val friendCode = binding.etFriendCode.text.toString()
            if (isValidUsercode(friendCode)) {
                DeviceUtils.hideKeyboard(requireActivity())
                binding.etFriendCode.error = null
                viewModel.sendFriendRequest(friendCode)
            }
        }

        viewModel.loadUserCode()
        viewModel.loadFriendRequestsSent(userId)
    }

    private fun isValidUsercode(friendCode: String) = try {
        if (friendCode.length < 3) throw NumberFormatException()
        if (friendCode == binding.tvYourCode.text) throw NumberFormatException()

        isNewUserCode(friendCode)
    } catch (nfe: NumberFormatException) {
        binding.etFriendCode.error = getString(R.string.error_invalid_friendcode)
        false
    }

    private fun isNewUserCode(friendCode: String): Boolean {
        val isDuplicateRequest = sentFriendRequests.any { it.receiverCode == friendCode }
        if (isDuplicateRequest) {
            binding.etFriendCode.error = getString(R.string.error_friend_request_already_sent)
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

