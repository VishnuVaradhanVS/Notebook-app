package com.example.notebook



data class User(var userName: String, var userPassword: String) {
    constructor() : this("", "")
}
