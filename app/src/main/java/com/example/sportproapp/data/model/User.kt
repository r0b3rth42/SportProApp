package com.example.sportproapp.data.model

data class User (
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.JUG,
    val status: UserStatus = UserStatus.ACTIVE,
    val guardianEmail: String? = "",
    val guardianId: String? = "",
    val photoUrl: String? = ""
)

enum class UserRole {
    DT,
    JUG,
    PAD,
    ADM
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}