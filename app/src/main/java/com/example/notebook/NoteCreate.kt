package com.example.notebook

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotes(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    navHostController: NavHostController,
    noteViewModel: NoteViewModel
) {
    val title = rememberSaveable {
        mutableStateOf(noteViewModel.currentNote.noteTitle)
    }
    val description = rememberSaveable {
        mutableStateOf(noteViewModel.currentNote.noteDescription)
    }
    val favorite = rememberSaveable {
        mutableStateOf(noteViewModel.currentNote.liked)
    }
    val currentuser = rememberSaveable {
        mutableStateOf(userViewModel.currentUserName)
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(title = {
                    Text("My Notes", color = MaterialTheme.colorScheme.primary)
                }, navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(onClick = {
                                navHostController.navigate(Screen.NotesDashboard.route)
                            })
                    )
                }, actions = {
                    Icon(
                        imageVector = if (favorite.value) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = "Favorites",
                        tint = if (favorite.value) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(
                                onClick = {
                                    favorite.value = !favorite.value
                                }))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(onClick = {
                                val db = FirebaseDB()
                                noteViewModel.currentNote.noteTitle = title.value
                                noteViewModel.currentNote.noteDescription = description.value
                                noteViewModel.currentNote.liked = favorite.value
                                noteViewModel.currentNote.recentAccess =
                                    LocalDateTime.now().toString()
                                coroutineScope.launch {
                                    val result = db.saveOrUpdateUserNote(
                                        currentuser.value, noteViewModel.currentNote
                                    )
                                    if (result) {
                                        Toast.makeText(
                                            context, "Note saved successfully", Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context, "Save failed", Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    navHostController.navigate(Screen.NotesDashboard.route)
                                }
                            })
                    )
                })
            },
        ) { values ->
            LazyColumn(modifier.padding(values)) {
                item {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp)
                    )
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        placeholder = { Text("Enter note title...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Text(
                        text = "Content",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp)
                    )
                    OutlinedTextField(
                        value = description.value,
                        onValueChange = { description.value = it },
                        placeholder = { Text("Write your note here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        minLines = 1,
                        maxLines = 1000
                    )
                }
            }
        }
    }
}