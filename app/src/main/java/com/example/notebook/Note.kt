package com.example.notebook

import java.time.LocalDateTime

data class Note(
    var noteid: String = "",
    var noteTitle: String = "Title",
    var noteDescription: String = "Description",
    var liked: Boolean = false,
    var recentAccess: String = LocalDateTime.now().toString()
) {
    constructor() : this("", "", "", false, LocalDateTime.now().toString())

}