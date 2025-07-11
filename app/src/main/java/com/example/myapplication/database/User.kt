package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // Nome completo do usuário
    val email: String,
    val password: String,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)