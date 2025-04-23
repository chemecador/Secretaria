package com.chemecador.secretaria.ui.view.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chemecador.secretaria.core.Constants
import com.chemecador.secretaria.ui.theme.SecretariaTheme
import com.chemecador.secretaria.ui.view.main.screens.NoteDetailScreen
import com.chemecador.secretaria.ui.view.main.screens.NotesListsScreen
import com.chemecador.secretaria.ui.view.main.screens.NotesScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecretariaTheme {
                val navController = rememberNavController()
                MainNavHost(navController = navController)
            }
        }
    }
    @Composable
    fun MainNavHost(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = Constants.NOTES_LIST
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
                    navArgument(Constants.LIST_ID) { type = NavType.Companion.StringType },
                    navArgument(Constants.LIST_NAME) { type = NavType.Companion.StringType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(Constants.LIST_ID)
                val listName = backStackEntry.arguments?.getString(Constants.LIST_NAME)
                NotesScreen(
                    listId = listId ?: "",
                    listName = listName ?: "Lista",
                    onNoteClick = { noteId ->
                        navController.navigate("note_detail/${listId ?: ""}/$noteId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "note_detail/{${Constants.LIST_ID}}/{${Constants.NOTE_ID}}",
                arguments = listOf(
                    navArgument(Constants.LIST_ID) { type = NavType.Companion.StringType },
                    navArgument(Constants.NOTE_ID) { type = NavType.Companion.StringType }
                )
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(Constants.LIST_ID)
                val noteId = backStackEntry.arguments?.getString(Constants.NOTE_ID)
                NoteDetailScreen(
                    listId = listId ?: "",
                    noteId = noteId ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

}