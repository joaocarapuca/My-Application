package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Entidade para membros dos grupos
 */
@Entity(
    tableName = "group_members",
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
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroupMember(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupId: Int, // ID do grupo
    val userId: Int, // ID do utilizador
    val joinedAt: Long = System.currentTimeMillis()
)