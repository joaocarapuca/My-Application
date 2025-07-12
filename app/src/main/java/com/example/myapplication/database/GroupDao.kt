package com.example.myapplication.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com grupos
 */
@Dao
interface GroupDao {
    
    @Insert
    suspend fun insertGroup(group: Group): Long
    
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<Group>>
    
    @Query("""
        SELECT g.* FROM groups g 
        INNER JOIN group_members gm ON g.id = gm.groupId 
        WHERE gm.userId = :userId
        ORDER BY g.name ASC
    """)
    fun getUserGroups(userId: Int): Flow<List<Group>>
    
    @Delete
    suspend fun deleteGroup(group: Group)
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Int): Group?
}

/**
 * DAO para membros dos grupos
 */
@Dao
interface GroupMemberDao {
    
    @Insert
    suspend fun insertGroupMember(groupMember: GroupMember)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeGroupMember(groupId: Int, userId: Int)
    
    @Query("""
        SELECT u.* FROM users u 
        INNER JOIN group_members gm ON u.id = gm.userId 
        WHERE gm.groupId = :groupId
        ORDER BY u.name ASC
    """)
    fun getGroupMembers(groupId: Int): Flow<List<User>>
    
    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun isUserInGroup(groupId: Int, userId: Int): Int
    
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
    
    @Insert
    suspend fun insertGroupMessage(groupMessage: GroupMessage)
    
    @Query("""
        SELECT gm.*, u.name as senderName, u.email as senderEmail 
        FROM group_messages gm 
        INNER JOIN users u ON gm.senderId = u.id 
        WHERE gm.groupId = :groupId 
        ORDER BY gm.timestamp ASC
    """)
    fun getGroupMessages(groupId: Int): Flow<List<GroupMessageWithSender>>
    
    @Query("UPDATE group_messages SET isRead = 1 WHERE groupId = :groupId")
    suspend fun markGroupMessagesAsRead(groupId: Int)
}