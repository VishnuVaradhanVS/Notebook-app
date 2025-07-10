package com.example.notebook

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(modifier: Modifier = Modifier, navHostController: NavHostController) {
    val username = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val confirmPassword = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val confirmPasswordVisible = rememberSaveable { mutableStateOf(false) }

    val showError = rememberSaveable { mutableStateOf(false) }
    val passwordMismatch = rememberSaveable { mutableStateOf(false) }
    val signupError = rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val validUserName = rememberSaveable { mutableStateOf(true) }
    val validUserPassword = rememberSaveable { mutableStateOf(true) }


    val scrollState = rememberScrollState()
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
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = username.value,
                        onValueChange = {
//                            coroutineScope.launch {
//                                val db = FirebaseDB()
//                                validUserName.value = db.validateUserName(it, context)
                            username.value = it
//                            }
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = "Username")
                        },
                        singleLine = true,
                        isError = !validUserName.value,
                        supportingText = {
                            if (!validUserName.value) Text(
                                text = "This username already exists",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        })
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password.value,
                        onValueChange = {
//                            coroutineScope.launch {
//                                val db = FirebaseDB()
//                                validUserPassword.value = db.validateUserPassword(it,context)
                            password.value = it
//                            }

                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Sharp.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = if (passwordVisible.value) Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                modifier = Modifier.clickable {
                                    passwordVisible.value = !passwordVisible.value
                                })
                        },
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = !validUserPassword.value,
                        supportingText = {
                            if (!validUserPassword.value) Text(
                                text = "Password must be at least 8 characters long and at least contain 1 uppercase letter,1 lowercase letter,1 digit,1 special character",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        })

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = confirmPassword.value,
                        onValueChange = { confirmPassword.value = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Sharp.Lock, contentDescription = "Confirm Password")
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = if (confirmPasswordVisible.value) Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle Confirm Password Visibility",
                                modifier = Modifier.clickable {
                                    confirmPasswordVisible.value = !confirmPasswordVisible.value
                                })
                        },
                        visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true
                    )

                    if (showError.value) {
                        Text(
                            text = "Please fill all fields",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (passwordMismatch.value) {
                        Text(
                            text = "Passwords do not match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (signupError.value) {
                        Text(
                            text = "Unable to Register, Try Again",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        signupError.value = false
                    }

                    Button(
                        onClick = {
                            showError.value =
                                username.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank()
                            passwordMismatch.value = password.value != confirmPassword.value

                            if (!showError.value && !passwordMismatch.value) {
                                // Handle successful signup
                                val db = FirebaseDB()
                                val newuser = User(
                                    userName = username.value, userPassword = password.value
                                )
                                coroutineScope.launch {
                                    validUserName.value =
                                        db.validateUserName(newuser.userName, context)
                                    validUserPassword.value =
                                        db.validateUserPassword(newuser.userPassword, context)
                                    if (validUserName.value && validUserPassword.value) {
                                        val result = db.addNewUser(newuser, context)
                                        if (result == true) {
                                            Toast.makeText(
                                                context,
                                                "Signed Up Successfully!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            navHostController.navigate(Screen.Login.route)
                                        } else {
                                            Toast.makeText(
                                                context, "Sign in Error", Toast.LENGTH_LONG
                                            ).show()
                                            signupError.value = true
                                        }
                                    }
                                }

                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Up")
                    }

                    OutlinedButton(
                        onClick = {
                            // Handle back to login
                            navHostController.navigate(Screen.Login.route)
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Login")
                    }
                }
            }
        }
    }
}