package com.example.notebook

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseDB {
    val db = FirebaseFirestore.getInstance()
    suspend fun addNewUser(user: User, context: Context): Boolean {
        return try {
            db.collection("usernotes").document(user.userName).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun validateUserName(username: String, context: Context): Boolean {
        return try {
            val docsnapshot = db.collection("usernotes").document(username).get().await()
            !docsnapshot.exists()
        } catch (e: Exception) {
            true
        }
    }

    suspend fun validateUserPassword(password: String, context: Context): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() } && password.any { it.isLowerCase() } && password.any { !it.isLetterOrDigit() }
    }

    suspend fun validateUserCredentials(user: User, context: Context): Boolean {
        return try {
            val userdata = db.collection("usernotes").document(user.userName).get().await()
            return user.userPassword == userdata.get("userPassword")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUser(username: String): User? {
        var currentUser: User?
        try {
            val userdata = db.collection("usernotes").document(username).get().await()
            currentUser = userdata.toObject(User::class.java)
            return currentUser
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getUserNotes(username: String): MutableList<Note>? {
        return try {
            val querySnapshot = db.collection("usernotes").document(username).collection("notes")
                .orderBy("recentAccess", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()

            val notes = mutableListOf<Note>()
            for (doc in querySnapshot.documents) {
                val note = doc.toObject(Note::class.java)
                if (note != null) {
                    note.noteid = doc.id
                    notes.add(note)
                }
            }
            notes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserNotesFav(username: String): MutableList<Note>? {
        return try {
            val querySnapshot = db.collection("usernotes").document(username).collection("notes")
                .whereEqualTo("liked", true)
                .orderBy("recentAccess", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()

            val notes = mutableListOf<Note>()
            for (doc in querySnapshot.documents) {
                val note = doc.toObject(Note::class.java)
                if (note != null) {
                    note.noteid = doc.id
                    notes.add(note)
                }
            }
            notes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun saveOrUpdateUserNote(username: String, note: Note): Boolean {
        return try {
            val notesRef = db.collection("usernotes").document(username).collection("notes")

            val docRef = if (note.noteid.isNullOrBlank()) {
                val newDoc = notesRef.document()
                note.noteid = newDoc.id  // important to assign
                newDoc
            } else {
                notesRef.document(note.noteid)
            }
            docRef.set(note).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}