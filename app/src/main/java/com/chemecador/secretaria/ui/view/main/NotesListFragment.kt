package com.chemecador.secretaria.ui.view.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.databinding.DialogConfirmDeleteBinding
import com.chemecador.secretaria.databinding.DialogCreateListBinding
import com.chemecador.secretaria.databinding.FragmentNotesListBinding
import com.chemecador.secretaria.ui.view.login.LoginActivity
import com.chemecador.secretaria.ui.view.main.MainActivity.Companion.TITLE_KEY
import com.chemecador.secretaria.ui.view.main.MainActivity.Companion.TITLE_REQUEST_KEY
import com.chemecador.secretaria.ui.view.rv.adapters.NotesListAdapter
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        initUI()
        observeViewModel()

        setFragmentResult(TITLE_REQUEST_KEY, Bundle().apply {
            putString(TITLE_KEY, getString(R.string.title_noteslist))
        })

    }

    private fun initUI() {
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
        adapter = NotesListAdapter(
            onListClick = { listId, name -> onListClick(listId, name) },
            onEditList = { updatedList -> editList(updatedList) },
            onDeleteList = { listId -> onDeleteList(listId) })

        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter
    }

    private fun onDeleteList(listId: String) {
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)
        dialogBinding.tvMsg.text = getString(R.string.label_confirm_delete_list)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .show()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnConfirm.setOnClickListener {
            viewModel.deleteList(listId).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Success -> {
                        Toast.makeText(context, R.string.label_list_deleted, Toast.LENGTH_SHORT)
                            .show()
                        binding.pb.isVisible = false
                    }

                    is Resource.Error -> {
                        binding.pb.isVisible = false
                        Toast.makeText(
                            context,
                            getString(R.string.error_deleting_list, result.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is Resource.Loading -> {
                        binding.pb.isVisible = true
                    }
                }
            }
            dialog.dismiss()
        }
    }

    private fun editList(currentList: NotesList) {
        val dialogBinding = DialogCreateListBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext()).create().apply {
            setView(dialogBinding.root)
            dialogBinding.etListName.setText(currentList.name)

            dialogBinding.btnOk.setOnClickListener {
                val newName = dialogBinding.etListName.text.toString()
                if (newName.isNotBlank()) {
                    dialogBinding.etListName.error = null
                    val updatedList = currentList.copy(name = newName)
                    viewModel.editList(updatedList)
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


    private fun onListClick(listId: String, name: String) {
        val fragment = NotesFragment().apply {
            arguments = Bundle().apply {
                putString(LIST_ID, listId)
                putString(LIST_NAME, name)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun observeViewModel() {
        viewModel.notesLists.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                    binding.tvEmpty.isVisible = false
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.tvEmpty.isVisible = resource.data.isNullOrEmpty()
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    binding.tvEmpty.isVisible = false
                    Toast.makeText(
                        context,
                        getString(R.string.error, resource.message),
                        Toast.LENGTH_LONG
                    ).show()
                    logout()
                }
            }
        }
        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                    binding.tvEmpty.isVisible = false
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(
                        context,
                        getString(R.string.error_updating_list, status.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun logout() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        const val LIST_ID = "listId"
        const val LIST_NAME = "listName"
    }
}
