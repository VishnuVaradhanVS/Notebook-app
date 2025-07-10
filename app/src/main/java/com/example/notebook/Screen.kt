package com.example.notebook

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object NotesDashboard : Screen("notesdashboard")
    object NoteCreate : Screen("notecreate")
}
