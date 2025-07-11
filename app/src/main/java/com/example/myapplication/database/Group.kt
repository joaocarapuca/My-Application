package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Group para grupos da aplicação
 */
@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // Nome do grupo (ex: "SI", "TW", "Design")
    val description: String = "", // Descrição do grupo
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: Int // ID do admin que criou
)