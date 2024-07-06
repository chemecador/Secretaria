package com.chemecador.secretaria.ui.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.DialogConfirmDeleteBinding
import com.chemecador.secretaria.databinding.FragmentNoteDetailBinding
import com.chemecador.secretaria.ui.view.main.NotesFragment.Companion.NOTE_ID
import com.chemecador.secretaria.ui.view.main.NotesListFragment.Companion.LIST_ID
import com.chemecador.secretaria.ui.viewmodel.main.NoteDetailViewModel
import com.chemecador.secretaria.utils.DateUtils
import com.chemecador.secretaria.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var listId: String
    private lateinit var note: Note

    private val viewModel: NoteDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.btnEdit.setOnClickListener {
            setEditionMode(true)
        }
        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
        binding.btnCancel.setOnClickListener {
            setEditionMode(false)
        }
        binding.btnConfirm.setOnClickListener {
            editNote()
        }
    }

    private fun editNote() {

        val title = binding.etTitle.text.toString()
        if (title.isBlank()) {
            binding.etTitle.requestFocus()
            binding.etTitle.error = getString(R.string.error_empty_field)
            return
        }
        note = note.copy(
            title = title,
            content = binding.etContent.text.toString()
        )
        viewModel.editNote(listId, note)
    }

    private fun showDeleteDialog() {
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .show()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnConfirm.setOnClickListener {
            viewModel.deleteNote(listId, note.id)
            dialog.dismiss()
        }
    }

    private fun setEditionMode(edit: Boolean) {
        binding.tvTitle.isVisible = !edit
        binding.tilTitle.isVisible = edit
        binding.tvContent.isVisible = !edit
        binding.tilContent.isVisible = edit

        binding.btnEdit.isVisible = !edit
        binding.btnDelete.isVisible = !edit

        binding.btnConfirm.isVisible = edit
        binding.btnCancel.isVisible = edit
    }

    private fun observeViewModel() {
        note = Note(
            id = requireArguments().getString(NOTE_ID, "")
        )
        listId = requireArguments().getString(LIST_ID, "")

        if (listId.isBlank() || note.id.isBlank()) {
            binding.tvError.isVisible = true
            return
        }

        viewModel.getNote(
            listId, note.id
        ).observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                    binding.content.isVisible = false
                    binding.tvError.isVisible = false
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.content.isVisible = true
                    binding.tvError.isVisible = false
                    resource.data?.let { note ->
                        this.note = note
                        bindNote()
                    }
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    binding.tvError.isVisible = true
                    binding.content.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }


        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    binding.pb.isVisible = false
                    setEditionMode(false)
                    bindNote()
                    Toast.makeText(
                        requireContext(),
                        R.string.label_note_updated,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_updating_note, status.message),
                        Toast.LENGTH_LONG
                    ).show()
                }

                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }
            }
        }

        viewModel.deleteStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    onDeleteSuccess()
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    Toast.makeText(
                        context,
                        getString(R.string.error_deleting_note, status.message),
                        Toast.LENGTH_LONG
                    ).show()
                }

                is Resource.Loading -> {
                    binding.pb.isVisible = true
                }
            }
        }
    }

    private fun onDeleteSuccess() {
        Toast.makeText(requireContext(), R.string.label_note_deleted, Toast.LENGTH_LONG).show()
        parentFragmentManager.popBackStack()
    }

    private fun bindNote() {

        // Set the title for this fragment
        setFragmentResult(MainActivity.TITLE_REQUEST_KEY, Bundle().apply {
            putString(
                MainActivity.TITLE_KEY,
                note.title
            )
        })

        binding.tvTitle.text = note.title
        binding.etTitle.setText(note.title)
        binding.tvContent.text = note.content
        binding.etContent.setText(note.content)
        binding.tvDate.text = note.date?.let { DateUtils.formatDetailed(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
