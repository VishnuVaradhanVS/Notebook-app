package com.example.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesDashboard(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    navHostController: NavHostController,
    noteViewModel: NoteViewModel
) {
    val db = FirebaseDB()
    var coroutineScope = rememberCoroutineScope()
    var currentUser = remember { mutableStateOf<User?>(null) }
    LaunchedEffect(currentUser.value?.userName) {
        currentUser.value = db.getUser(userViewModel.currentUserName)
    }
    var usernotes = remember { mutableStateOf<List<Note>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    val usernotesfav = remember { mutableStateOf(false) }

    LaunchedEffect(currentUser.value?.userName) {
        currentUser.value?.userName?.let { username ->
            isLoading.value = true
            usernotes.value = db.getUserNotes(username) ?: emptyList()
            isLoading.value = false
        }
    }
    LaunchedEffect(usernotesfav.value) {
        currentUser.value?.userName?.let { username ->
            isLoading.value = true
            usernotes.value = db.getUserNotesFav(username) ?: emptyList()
            isLoading.value = false
        }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(title = {
                Text("${currentUser.value?.userName}'s Notes")
            }, actions = {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorites",
                    tint = if (usernotesfav.value) Color.Red else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(onClick = {
                            usernotesfav.value = !usernotesfav.value
                        })

                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.padding(16.dp)
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                noteViewModel.currentNote = Note()
                navHostController.navigate(Screen.NoteCreate.route)
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new note")
            }
        }) { values ->
        when {
            isLoading.value -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            usernotes.value.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes found. Create your first note!")
                }
            }

            else -> {
                LazyColumn(modifier = modifier.padding(values)) {
                    items(usernotes.value.size) { index ->
                        NoteCard(
                            note = usernotes.value[index], modifier = Modifier.clickable(
                            onClick = {
                                noteViewModel.currentNote = usernotes.value[index]
                                val updatedList = usernotes.value.toMutableList()
                                val currentNote = updatedList[index]
                                val updatedNote = currentNote.copy(
                                    noteid = currentNote.noteid,
                                    recentAccess = LocalDateTime.now().toString()
                                )
                                updatedList[index] = updatedNote
                                usernotes.value = updatedList
                                coroutineScope.launch {
                                    db.saveOrUpdateUserNote(
                                        currentUser.value?.userName ?: "", updatedNote
                                    )
                                }
                                navHostController.navigate(Screen.NoteCreate.route)
                            }), onFavoriteToggle = {
                            val updatedList = usernotes.value.toMutableList()
                            val currentNote = updatedList[index]
                            val updatedNote = currentNote.copy(
                                isLiked = !currentNote.isLiked, noteid = currentNote.noteid
                            )
                            updatedList[index] = updatedNote
                            usernotes.value = updatedList
                            coroutineScope.launch {
                                db.saveOrUpdateUserNote(
                                    currentUser.value?.userName ?: "", updatedNote
                                )
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(modifier: Modifier = Modifier, note: Note, onFavoriteToggle: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                note.noteTitle,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                modifier = Modifier.clickable {
                    onFavoriteToggle()
                },
                tint = if (note.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            note.noteDescription,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}