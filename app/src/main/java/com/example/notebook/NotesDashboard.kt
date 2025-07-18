package com.example.notebook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    LaunchedEffect(currentUser.value?.userName,usernotesfav.value) {
        currentUser.value?.userName?.let { username ->
            isLoading.value = true
            usernotes.value = if (usernotesfav.value) {
                db.getUserNotesFav(username) ?: emptyList()
            } else {
                db.getUserNotes(username) ?: emptyList()
            }
            isLoading.value = false
        }
    }

    var selectionMode = remember { mutableStateOf(false) }
    var selectedNotes = remember { mutableStateOf(setOf<String>()) }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(title = {
                if (!selectionMode.value) Text("${currentUser.value?.userName}'s Notes")
            },colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
                navigationIcon = {
                if (selectionMode.value) Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(onClick = {
                            selectionMode.value = false
                            selectedNotes.value = setOf<String>()
                        })
                )
            }, actions = {
                if (!selectionMode.value) Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorites",
                    tint = if (usernotesfav.value) Color.Red else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(onClick = {
                            usernotesfav.value=!usernotesfav.value
                        })
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(onClick = {
                            coroutineScope.launch {
                                val username = currentUser.value?.userName ?: return@launch
                                val result = db.deleteUserNotes(
                                    username,
                                    selectedNotes.value
                                )
                                if (result) {
                                    usernotes.value =
                                        db.getUserNotes(currentUser.value?.userName.toString())
                                            ?: emptyList()
                                }
                                selectionMode.value = false
                                selectedNotes.value = emptySet<String>()
                            }

                        })
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                noteViewModel.currentNote = Note()
                navHostController.navigate(Screen.NoteCreate.route)
            }, containerColor = MaterialTheme.colorScheme.primary) {
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
                            note = usernotes.value[index],
                            modifier = Modifier.combinedClickable(onClick = {
                                if (!selectionMode.value) {
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
                                } else {
                                    selectedNotes.value = selectedNotes.value.toMutableSet().apply {
                                        if (contains(usernotes.value[index].noteid)) remove(
                                            usernotes.value[index].noteid
                                        ) else add(usernotes.value[index].noteid)
                                    }
                                    if (selectedNotes.value.isEmpty()) {
                                        selectionMode.value = false
                                        selectedNotes.value = mutableSetOf<String>()
                                    }
                                }
                            }, onLongClick = {
                                selectionMode.value = true
                                selectedNotes.value =
                                    selectedNotes.value + usernotes.value[index].noteid

                            }),
                            onFavoriteToggle = {
                                val updatedList = usernotes.value.toMutableList()
                                val currentNote = updatedList[index]
                                val updatedNote = currentNote.copy(
                                    liked = !currentNote.liked, noteid = currentNote.noteid
                                )
                                updatedList[index] = updatedNote
                                usernotes.value = updatedList
                                coroutineScope.launch {
                                    db.saveOrUpdateUserNote(
                                        currentUser.value?.userName ?: "", updatedNote
                                    )
                                }
                            },
                            isSelected = selectedNotes.value.contains(usernotes.value[index].noteid),
                            isSelectionMode = selectionMode.value
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    onFavoriteToggle: () -> Unit,
    isSelected: Boolean,
    isSelectionMode: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
        ),
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
                tint = if (note.liked) Color.Red else MaterialTheme.colorScheme.onSurface
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