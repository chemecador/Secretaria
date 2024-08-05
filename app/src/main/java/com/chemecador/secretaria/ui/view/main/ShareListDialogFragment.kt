package com.chemecador.secretaria.ui.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.constants.Constants.LIST_ID
import com.chemecador.secretaria.databinding.FragmentShareListBinding
import com.chemecador.secretaria.ui.view.rv.adapters.main.ContributorAdapter
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareListDialogFragment : DialogFragment() {

    private var _binding: FragmentShareListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesListViewModel by viewModels()
    private val friendsViewModel: FriendsViewModel by viewModels()
    private lateinit var adapter: ContributorAdapter
    private lateinit var listId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listId = it.getString(LIST_ID) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContributorAdapter { friendId ->
            viewModel.shareListWithFriend(listId, friendId)
        }
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter

        observeViewModel()
        friendsViewModel.loadFriends()
    }

    private fun observeViewModel() {
        friendsViewModel.friends.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.tvEmpty.isVisible = resource.data.isNullOrEmpty()
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    binding.tvEmpty.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.shareListStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, R.string.label_list_shared, Toast.LENGTH_SHORT).show()
                    dismiss()
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(listId: String) =
            ShareListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(LIST_ID, listId)
                }
            }
    }
}
