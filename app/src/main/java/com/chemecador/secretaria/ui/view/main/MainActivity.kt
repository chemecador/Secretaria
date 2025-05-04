package com.chemecador.secretaria.ui.view.main


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.Constants
import com.chemecador.secretaria.ui.theme.SecretariaTheme
import com.chemecador.secretaria.ui.view.friends.FriendsScreen
import com.chemecador.secretaria.ui.view.main.screens.NoteDetailScreen
import com.chemecador.secretaria.ui.view.main.screens.NotesListsScreen
import com.chemecador.secretaria.ui.view.main.screens.NotesScreen
import com.chemecador.secretaria.ui.view.settings.AboutUsScreen
import com.chemecador.secretaria.ui.view.settings.SettingsScreen
import com.chemecador.secretaria.ui.viewmodel.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecretariaTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val route = backStackEntry?.destination?.route
                val appBarTitle = when (route) {

                    Constants.NOTES_LIST -> stringResource(R.string.title_noteslist)

                    "${Constants.NOTES}/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}" ->
                        backStackEntry?.arguments?.getString(Constants.LIST_NAME)
                            ?: stringResource(R.string.title_noteslist)

                    "note_detail/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}/{${Constants.NOTE_ID}}" ->
                        backStackEntry?.arguments?.getString(Constants.LIST_NAME)
                            ?: stringResource(R.string.title_note_detail)

                    Constants.ABOUT_US -> stringResource(R.string.title_activity_about_us)

                    Constants.FRIENDS -> stringResource(R.string.label_friends)

                    Constants.SETTINGS -> stringResource(R.string.title_activity_settings)
                    else -> stringResource(R.string.app_name)
                }

                Scaffold(
                    topBar = { SecretariaTopBar(navController, appBarTitle) }
                ) { padding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding)
                    ) {
                        MainNavHost(navController)
                    }
                }
            }
        }
    }

    @Composable
    fun MainNavHost(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = Constants.NOTES_LIST,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Constants.NOTES_LIST) {
                NotesListsScreen(
                    onListClick = { listId, listName ->
                        navController.navigate("${Constants.NOTES}/$listId/$listName")
                    }
                )
            }
            composable(
                route = "${Constants.NOTES}/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}",
                arguments = listOf(
                    navArgument(Constants.LIST_ID) { type = NavType.StringType },
                    navArgument(Constants.LIST_NAME) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(Constants.LIST_ID) ?: ""
                val listName = backStackEntry.arguments?.getString(Constants.LIST_NAME) ?: ""
                NotesScreen(
                    listId = listId,
                    onNoteClick = { noteId ->
                        navController.navigate("note_detail/${listId}/${listName}/$noteId")
                    },
                )
            }
            composable(
                route = "note_detail/{${Constants.LIST_ID}}/{${Constants.LIST_NAME}}/{${Constants.NOTE_ID}}",
                arguments = listOf(
                    navArgument(Constants.LIST_ID) { type = NavType.StringType },
                    navArgument(Constants.LIST_NAME) { type = NavType.StringType },
                    navArgument(Constants.NOTE_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(Constants.LIST_ID) ?: ""
                val noteId = backStackEntry.arguments?.getString(Constants.NOTE_ID) ?: ""
                NoteDetailScreen(
                    listId = listId,
                    noteId = noteId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Constants.SETTINGS) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = settingsViewModel)
            }
            composable(Constants.ABOUT_US) {
                val email = stringResource(R.string.contact_mail)
                val mailSubject = stringResource(R.string.label_mail_subject)
                AboutUsScreen(
                    onGithubClick = { url ->
                        startActivity(
                            Intent(Intent.ACTION_VIEW, url.toUri())
                        )
                    },
                    onContactClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, email)
                            putExtra(Intent.EXTRA_SUBJECT, mailSubject)
                        }
                        startActivity(intent)
                    }
                )
            }
            composable(Constants.FRIENDS) {
                FriendsScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SecretariaTopBar(navController: NavHostController, title: String) {
        val canPop = navController.previousBackStackEntry != null
        val expanded = remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                if (canPop) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate(Constants.FRIENDS) }) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = stringResource(R.string.label_friends)
                    )
                }

                IconButton(onClick = { expanded.value = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title_activity_settings)) },
                        onClick = {
                            expanded.value = false
                            navController.navigate(Constants.SETTINGS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title_activity_about_us)) },
                        onClick = {
                            expanded.value = false
                            navController.navigate(Constants.ABOUT_US)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
