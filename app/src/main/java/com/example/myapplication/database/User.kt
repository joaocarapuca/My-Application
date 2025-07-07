package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val password: String,
    val isAdmin: Boolean = false, // Campo para identificar administradores
    val createdAt: Long = System.currentTimeMillis() // Data de criação
) {
    // Se você tiver outro construtor que não quer que o Room use, anote com @Ignore
    // Por exemplo:
    /*
    @Ignore
    constructor(email: String, password: String) : this(0, email, password, false, System.currentTimeMillis())
    */
}
