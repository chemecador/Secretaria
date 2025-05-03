package com.chemecador.secretaria.ui.view.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.ui.viewmodel.friends.FriendsViewModel
import com.chemecador.secretaria.ui.viewmodel.main.NotesListViewModel
import com.chemecador.secretaria.utils.Resource

@Composable
fun ShareListDialog(
    listId: String,
    onDismissRequest: () -> Unit,
    viewModel: NotesListViewModel = hiltViewModel(),
    friendsViewModel: FriendsViewModel = hiltViewModel()
) {
    val friendsResource by friendsViewModel.friends.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        friendsViewModel.loadFriends()
    }
    LaunchedEffect(Unit) {
        viewModel.shareListStatus.collect { status ->
            when (status) {
                is Resource.Success -> {
                    Toast.makeText(context, R.string.label_list_shared, Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                }

                is Resource.Error -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }

                else -> { /* Loading → do nothing */ }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.action_share)) },
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(200.dp, 400.dp)
            ) {
                when (friendsResource) {
                    is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is Resource.Error -> Text(
                        text = (friendsResource as Resource.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    is Resource.Success -> {
                        val list = (friendsResource as Resource.Success<List<Friend>>).data
                        if (list.isNullOrEmpty()) {
                            Text(
                                stringResource(R.string.label_no_friends),
                                Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn {
                                items(list) { friend ->
                                    ContributorItem(friend) {
                                        viewModel.shareListWithFriend(listId, friend.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}


@Composable
fun ContributorItem(
    friend: Friend,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onShareClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.name, style = MaterialTheme.typography.titleMedium)
                Text(text = friend.email, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onShareClick) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir")
            }
        }
    }
}
