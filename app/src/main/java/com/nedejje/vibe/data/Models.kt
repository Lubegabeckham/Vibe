package com.nedejje.vibe.data

import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val time: String = "",
    val location: String,
    val description: String = "",
    val category: String = "OTHER",
    val priceOrdinary: Long = 0,
    val priceVIP: Long = 0,
    val priceVVIP: Long = 0,
    val isFree: Boolean = false,
    val capacity: Long = 0
)

enum class UserRole {
    USER, ADMIN
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val role: UserRole = UserRole.USER
)
