package com.chemecador.secretaria.ui.view.main

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
import com.chemecador.secretaria.databinding.FragmentNotesListBinding
import com.chemecador.secretaria.ui.view.rv.adapters.NotesListAdapter
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesListViewModel by viewModels()
    private lateinit var adapter: NotesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotesListAdapter { listId ->
            Toast.makeText(requireContext(), "List ID: $listId", Toast.LENGTH_SHORT).show()
        }

        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter

        // TODO: get UID
        val userId = ""

        if (userId.isNullOrBlank()) return
        viewModel.getNotesLists(userId).observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                }
            }
            if (resource.data.isNullOrEmpty()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.label_empty_data),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
