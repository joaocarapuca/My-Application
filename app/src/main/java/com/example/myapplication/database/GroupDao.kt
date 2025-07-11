package com.example.myapplication.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com grupos
 */
@Dao
interface GroupDao {
    
    /**
     * Inserir novo grupo
     */
    @Insert
    suspend fun insertGroup(group: Group): Long
    
    /**
     * Obter todos os grupos
     */
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<Group>>
    
    /**
     * Obter grupos de um utilizador
     */
    @Query("""
        SELECT g.* FROM groups g 
        INNER JOIN group_members gm ON g.id = gm.groupId 
        WHERE gm.userId = :userId
        ORDER BY g.name ASC
    """)
    fun getUserGroups(userId: Int): Flow<List<Group>>
    
    /**
     * Eliminar grupo
     */
    @Delete
    suspend fun deleteGroup(group: Group)
    
    /**
     * Obter grupo por ID
     */
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Int): Group?
}

/**
 * DAO para membros dos grupos
 */
@Dao
interface GroupMemberDao {
    
    /**
     * Adicionar membro ao grupo
     */
    @Insert
    suspend fun insertGroupMember(groupMember: GroupMember)
    
    /**
     * Remover membro do grupo
     */
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeGroupMember(groupId: Int, userId: Int)
    
    /**
     * Obter membros de um grupo
     */
    @Query("""
        SELECT u.* FROM users u 
        INNER JOIN group_members gm ON u.id = gm.userId 
        WHERE gm.groupId = :groupId
        ORDER BY u.name ASC
    """)
    fun getGroupMembers(groupId: Int): Flow<List<User>>
    
    /**
     * Verificar se utilizador é membro do grupo
     */
    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun isUserInGroup(groupId: Int, userId: Int): Int
    
    /**
     * Obter todos os membros de um grupo (sem Flow)
     */
    @Query("""
        SELECT u.* FROM users u 
        INNER JOIN group_members gm ON u.id = gm.userId 
        WHERE gm.groupId = :groupId
        ORDER BY u.name ASC
    """)
    suspend fun getGroupMembersList(groupId: Int): List<User>
}

/**
 * DAO para mensagens dos grupos
 */
@Dao
interface GroupMessageDao {
    
    /**
     * Inserir mensagem no grupo
     */
    @Insert
    suspend fun insertGroupMessage(groupMessage: GroupMessage)
    
    /**
     * Obter mensagens de um grupo
     */
    @Query("""
        SELECT gm.*, u.name as senderName, u.email as senderEmail 
        FROM group_messages gm 
        INNER JOIN users u ON gm.senderId = u.id 
        WHERE gm.groupId = :groupId 
        ORDER BY gm.timestamp ASC
    """)
    fun getGroupMessages(groupId: Int): Flow<List<GroupMessageWithSender>>
    
    /**
     * Marcar mensagens como lidas
     */
    @Query("UPDATE group_messages SET isRead = 1 WHERE groupId = :groupId")
    suspend fun markGroupMessagesAsRead(groupId: Int)
}

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