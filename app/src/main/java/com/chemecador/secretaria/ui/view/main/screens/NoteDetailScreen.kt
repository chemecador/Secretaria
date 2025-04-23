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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemecador.secretaria.data.model.Note
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (noteResource) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is Resource.Error -> {
                    Text(
                        text = "Error: ${(noteResource as Resource.Error).message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is Resource.Success -> {
                    val note = (noteResource as Resource.Success<Note>).data
                    if (note == null) {
                        Text(
                            text = "Nota no encontrada",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val dateString = remember(note.date) {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    sdf.format(note.date.toDate())
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

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!editMode) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            } else {
                                OutlinedTextField(
                                    value = titleText,
                                    onValueChange = { titleText = it },
                                    label = { Text("Título") },
                                    isError = titleText.isBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!editMode) {
                                Text(
                                    text = contentText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            } else {
                                OutlinedTextField(
                                    value = contentText,
                                    onValueChange = { contentText = it },
                                    label = { Text("Contenido") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 150.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = checkboxState,
                                    onCheckedChange = {
                                        checkboxState = it
                                        editMode = true
                                    }
                                )
                                Text(
                                    text = if (checkboxState) "Completada" else "No completada",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!editMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { editMode = true }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar")
                                    }
                                    Button(
                                        onClick = { showDeleteDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eliminar")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            editMode = false
                                            titleText = note.title
                                            contentText = note.content
                                            checkboxState = note.completed
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cancelar")
                                    }
                                    Button(
                                        onClick = {
                                            if (titleText.isBlank()) {
                                                return@Button
                                            }
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
                                        Icon(Icons.Default.Check, contentDescription = "Confirmar")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Confirmar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar nota") },
            text = { Text("¿Estás seguro de eliminar esta nota?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(listId, noteId)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
