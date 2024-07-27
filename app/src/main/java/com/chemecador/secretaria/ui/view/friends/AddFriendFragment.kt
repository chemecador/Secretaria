package com.chemecador.secretaria.ui.view.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentAddFriendBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    private fun setupUI() {
        binding.btnSendRequest.setOnClickListener {
            val friendCode = binding.etFriendCode.text.toString()
            if (isValidUsercode(friendCode)) {
                DeviceUtils.hideKeyboard(requireActivity())
                binding.etFriendCode.error = null
                viewModel.sendFriendRequest(friendCode)
            }
        }

        viewModel.loadUserCode()
    }

    private fun isValidUsercode(friendCode: String) = try {
        if (friendCode.length < 3) throw NumberFormatException()
        if (friendCode == binding.tvYourCode.text) throw NumberFormatException()

        friendCode.toInt()
        true
    } catch (nfe: NumberFormatException) {
        binding.etFriendCode.error = getString(R.string.error_invalid_friendcode)
        false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
