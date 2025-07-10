package com.example.notebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notebook.ui.theme.NoteBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userViewModel: UserViewModel = UserViewModel()
        val noteViewModel: NoteViewModel = NoteViewModel()
        setContent {
            NoteBookTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.Login.route) {
                    composable(Screen.Login.route) {
                        LoginScreen(modifier = Modifier, navController, userViewModel)
                    }
                    composable(Screen.Signup.route) {
                        SignUpScreen(modifier = Modifier, navController)
                    }
                    composable(Screen.NotesDashboard.route) {
                        NotesDashboard(
                            modifier = Modifier,
                            userViewModel,
                            navController,
                            noteViewModel
                        )
                    }
                    composable(Screen.NoteCreate.route) {
                        CreateNotes(
                            modifier = Modifier,
                            userViewModel,
                            navController,
                            noteViewModel
                        )
                    }

                }
            }
        }
    }
}




