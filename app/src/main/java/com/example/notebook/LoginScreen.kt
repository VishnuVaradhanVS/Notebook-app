package com.example.notebook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    userViewModel: UserViewModel
) {
    val username = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordVisibility = rememberSaveable { mutableStateOf(false) }
    val showError = rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val usernameExists = rememberSaveable { mutableStateOf(true) }
    val passwordMatch = rememberSaveable { mutableStateOf(true) }
    val loggedin = rememberSaveable { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App title
                    Text(
                        text = "NOTES",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            shadow = Shadow(
                                color = if (!isSystemInDarkTheme()) Color.Black.copy(alpha = 0.25f) else Color.White.copy(
                                    alpha = 0.3f
                                ), offset = Offset(2f, 2f), blurRadius = 4f
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Username field
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = username.value,
                        onValueChange = { username.value = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person, contentDescription = "Username"
                            )
                        },
                        singleLine = true,
                        isError = !usernameExists.value,
                        supportingText = {
                            if (!usernameExists.value) Text(
                                text = "This username does not exists",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        })

                    // Password field
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Sharp.Lock, contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = if (passwordVisibility.value) Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                modifier = Modifier.clickable {
                                    passwordVisibility.value = !passwordVisibility.value
                                })
                        },
                        visualTransformation = if (passwordVisibility.value) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = !passwordMatch.value,
                        supportingText = {
                            if (!passwordMatch.value) Text(
                                text = "Incorrect password",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        })

                    // Show error if fields are empty
                    if (showError.value) {
                        Text(
                            text = "Please enter both username and password",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (!loggedin.value) {
                        CircularProgressIndicator()
                    }
                    // Login Button
                    Button(
                        onClick = {
                            showError.value = username.value.isBlank() || password.value.isBlank()
                            if (!showError.value) {
                                // Handle login
                                coroutineScope.launch {
                                    loggedin.value = !loggedin.value
                                    val db = FirebaseDB()
                                    usernameExists.value =
                                        !db.validateUserName(username.value, context)
                                    passwordMatch.value = db.validateUserCredentials(
                                        User(
                                            username.value, password.value
                                        ), context
                                    )
                                    loggedin.value = !loggedin.value

                                    if (usernameExists.value && passwordMatch.value) {
                                        userViewModel.currentUserName = username.value
                                        navHostController.navigate(Screen.NotesDashboard.route)
                                    }
                                }

                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }

                    // Sign Up Button
                    OutlinedButton(
                        onClick = {
                            // Handle signup
                            navHostController.navigate(Screen.Signup.route)
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Up")
                    }
                }
            }
        }
    }
}