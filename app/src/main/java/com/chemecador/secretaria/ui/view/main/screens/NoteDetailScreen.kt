package com.chemecador.secretaria.ui.view.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.ui.view.components.ConfirmationDialog
import com.chemecador.secretaria.ui.view.components.SecretariaCheckbox
import com.chemecador.secretaria.ui.viewmodel.main.NoteDetailViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    listId: String,
    noteId: String,
    viewModel: NoteDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    LaunchedEffect(noteId, listId) {
        viewModel.getNote(listId, noteId)
    }

    val noteResource by viewModel.note.observeAsState(initial = Resource.Loading())

    var editMode by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var checkboxState by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteResource) {
        if (noteResource is Resource.Success) {
            (noteResource as Resource.Success<Note>).data?.let { note ->
                if (!editMode) {
                    titleText = note.title
                    contentText = note.content
                    checkboxState = note.completed
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (noteResource) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is Resource.Error -> {
                    Text(
                        text = stringResource(
                            R.string.error_generic,
                            (noteResource as Resource.Error).message.orEmpty()
                        ),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is Resource.Success -> {
                    val note = (noteResource as Resource.Success<Note>).data
                    if (note == null) {
                        Text(
                            text = stringResource(R.string.error_note_not_found),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensionResource(R.dimen.margin_medium))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val dateString = remember(note.date) {
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(note.date.toDate())
                                }
                                Text(
                                    text = dateString,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = note.creator,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            if (!editMode) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            } else {
                                OutlinedTextField(
                                    value = titleText,
                                    onValueChange = { titleText = it },
                                    label = { Text(stringResource(R.string.label_note_title)) },
                                    isError = titleText.isBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (titleText.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.error_empty_field),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            top = dimensionResource(R.dimen.margin_xsmall)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            if (!editMode) {
                                Text(
                                    text = contentText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            } else {
                                OutlinedTextField(
                                    value = contentText,
                                    onValueChange = { contentText = it },
                                    label = { Text(stringResource(R.string.label_note_content)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = dimensionResource(R.dimen.margin_xxlarge)),
                                    maxLines = 5
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SecretariaCheckbox(
                                    checked = checkboxState,
                                    onCheckedChange = {
                                        checkboxState = it
                                        editMode = true
                                    }
                                )
                                Text(
                                    text = if (checkboxState) stringResource(R.string.label_completed) else stringResource(
                                        R.string.label_not_completed
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = dimensionResource(R.dimen.margin_small))
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_large)))


                            if (!editMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OutlinedButton(
                                        onClick = { editMode = true },
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.action_edit)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_edit))
                                    }
                                    OutlinedButton(
                                        onClick = { showDeleteDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.action_delete)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_delete))
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            editMode = false
                                            noteResource.data?.let {
                                                titleText = it.title
                                                contentText = it.content
                                                checkboxState = it.completed
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.action_cancel)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (titleText.isBlank()) return@OutlinedButton
                                            val updatedNote = note.copy(
                                                title = titleText,
                                                content = contentText,
                                                date = Timestamp.now(),
                                                creator = viewModel.getUsername(),
                                                completed = checkboxState
                                            )
                                            viewModel.editNote(listId, updatedNote)
                                            editMode = false
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = stringResource(R.string.action_confirm)
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.margin_xsmall)))
                                        Text(stringResource(R.string.action_confirm))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.dialog_delete_note_title),
                text = stringResource(R.string.dialog_delete_note_msg),
                onConfirm = {
                    viewModel.deleteNote(listId, noteId)
                    showDeleteDialog = false
                    onBack()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}
