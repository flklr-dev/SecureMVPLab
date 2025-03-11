package com.example.securemvp.data.model

data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
) 