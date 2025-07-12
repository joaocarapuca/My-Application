package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Entidade para mensagens dos grupos
 */
@Entity(
    tableName = "group_messages",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroupMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupId: Int, // ID do grupo
    val senderId: Int, // ID do utilizador que enviou
    val content: String, // Conteúdo da mensagem
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * Classe para mensagens com informação do remetente
 */
data class GroupMessageWithSender(
    val id: Int,
    val groupId: Int,
    val senderId: Int,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean,
    val senderName: String,
    val senderEmail: String
)