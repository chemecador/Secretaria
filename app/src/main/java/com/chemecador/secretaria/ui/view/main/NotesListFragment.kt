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
import com.chemecador.secretaria.databinding.DialogCreateListBinding
import com.chemecador.secretaria.databinding.FragmentNotesListBinding
import com.chemecador.secretaria.ui.view.rv.adapters.NotesListAdapter
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        initUI()
        observeViewModel()

    }

    private fun initUI() {
        initRV()

        binding.fab.setOnClickListener {
            showCreateListDialog()
        }
    }

    private fun showCreateListDialog() {
        val dialogBinding = DialogCreateListBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext()).create().apply {
            setView(dialogBinding.root)
            dialogBinding.btnOk.setOnClickListener {
                val listName = dialogBinding.etListName.text.toString()
                if (listName.isNotBlank()) {
                    dialogBinding.etListName.error = null
                    viewModel.createList(listName)
                    dismiss()
                } else {
                    dialogBinding.etListName.requestFocus()
                    dialogBinding.etListName.error = getString(R.string.error_empty_field)
                }
            }
            dialogBinding.btnCancel.setOnClickListener {
                dismiss()
            }
        }
        dialog.show()
    }
    private fun initRV() {
        adapter = NotesListAdapter { listId ->
            Toast.makeText(requireContext(), "List ID: $listId", Toast.LENGTH_SHORT).show()
        }
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter
    }


    private fun observeViewModel() {
        viewModel.notesLists.observe(viewLifecycleOwner) { resource ->
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
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }

            binding.tvEmpty.isVisible = resource.data.isNullOrEmpty()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
