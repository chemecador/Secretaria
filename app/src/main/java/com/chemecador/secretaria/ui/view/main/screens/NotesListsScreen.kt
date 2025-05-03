package com.chemecador.secretaria.ui.view.main.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.ui.view.components.CascadingDropdownMenu
import com.chemecador.secretaria.ui.view.components.ConfirmationDialog
import com.chemecador.secretaria.ui.view.components.CreateListDialog
import com.chemecador.secretaria.ui.view.components.ShareListDialog
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListsScreen(
    viewModel: NotesListViewModel = hiltViewModel(),
    onListClick: (listId: String, listName: String) -> Unit = { _, _ -> }
) {

    var showDialog by remember { mutableStateOf(false) }

    val notesLists by viewModel.notesLists.collectAsState()
    var shareDialogListId by remember { mutableStateOf<String?>(null) }
    var deleteDialogListId by remember { mutableStateOf<String?>(null) }
    var editDialogData by remember { mutableStateOf<NotesList?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.label_create_list),
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = notesLists) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is Resource.Success -> {
                    val lists = state.data.orEmpty()
                    if (lists.isEmpty()) {
                        Text(
                            text = stringResource(R.string.label_empty_lists),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lists) { list ->
                                val dateString = remember(list.date) {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    sdf.format(list.date.toDate())
                                }
                                NotesListItem(
                                    title = list.name,
                                    creator = list.creator,
                                    date = dateString,
                                    onItemClick = { onListClick(list.id, list.name) },
                                    onShare = { shareDialogListId = list.id },
                                    onEdit = { editDialogData = list },
                                    onDelete = { deleteDialogListId = list.id }
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        CreateListDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onCreate = { name -> viewModel.createList(name) }
        )
    }

    shareDialogListId?.let { listId ->
        ShareListDialog(
            listId = listId,
            onDismissRequest = { shareDialogListId = null }
        )
    }


    deleteDialogListId?.let { listId ->
        ConfirmationDialog(
            title = stringResource(R.string.dialog_delete_list_title),
            text = stringResource(R.string.dialog_delete_list_msg),
            onConfirm = {
                viewModel.deleteList(listId)
                deleteDialogListId = null
            },
            onDismiss = { deleteDialogListId = null }
        )
    }

    editDialogData?.let { currentList ->
        CreateListDialog(
            showDialog = true,
            initialName = currentList.name,
            onDismiss = { editDialogData = null },
            onCreate = { newName ->
                viewModel.editList(currentList.copy(name = newName))
                editDialogData = null
            }
        )
    }
}

@Composable
fun NotesListItem(
    title: String,
    creator: String,
    date: String,
    onItemClick: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_small)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.cardview_default_elevation)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.margin_medium))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.label_more)
                        )
                    }
                    CascadingDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onShare = onShare,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.margin_small)))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = creator,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

