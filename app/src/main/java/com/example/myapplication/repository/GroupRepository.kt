package com.example.myapplication.repository

import com.example.myapplication.database.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositório para gerir grupos
 */
class GroupRepository(private val database: AppDatabase) {
    
    private val groupDao = database.groupDao()
    private val groupMemberDao = database.groupMemberDao()
    private val groupMessageDao = database.groupMessageDao()
    
    /**
     * Criar novo grupo
     */
    suspend fun createGroup(name: String, description: String, createdBy: Int): Long {
        val group = Group(
            name = name,
            description = description,
            createdBy = createdBy
        )
        return groupDao.insertGroup(group)
    }
    
    /**
     * Obter todos os grupos
     */
    fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups()
    }
    
    /**
     * Obter grupos do utilizador
     */
    fun getUserGroups(userId: Int): Flow<List<Group>> {
        return groupDao.getUserGroups(userId)
    }
    
    /**
     * Adicionar membro ao grupo
     */
    suspend fun addMemberToGroup(groupId: Int, userId: Int): Boolean {
        return try {
            if (groupMemberDao.isUserInGroup(groupId, userId) == 0) {
                val groupMember = GroupMember(groupId = groupId, userId = userId)
                groupMemberDao.insertGroupMember(groupMember)
                true
            } else {
                false // Já é membro
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Remover membro do grupo
     */
    suspend fun removeMemberFromGroup(groupId: Int, userId: Int): Boolean {
        return try {
            groupMemberDao.removeGroupMember(groupId, userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obter membros do grupo
     */
    fun getGroupMembers(groupId: Int): Flow<List<User>> {
        return groupMemberDao.getGroupMembers(groupId)
    }
    
    /**
     * Obter membros do grupo (lista)
     */
    suspend fun getGroupMembersList(groupId: Int): List<User> {
        return groupMemberDao.getGroupMembersList(groupId)
    }
    
    /**
     * Enviar mensagem no grupo
     */
    suspend fun sendGroupMessage(groupId: Int, senderId: Int, content: String): Boolean {
        return try {
            val message = GroupMessage(
                groupId = groupId,
                senderId = senderId,
                content = content
            )
            groupMessageDao.insertGroupMessage(message)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obter mensagens do grupo
     */
    fun getGroupMessages(groupId: Int): Flow<List<GroupMessageWithSender>> {
        return groupMessageDao.getGroupMessages(groupId)
    }
    
    /**
     * Eliminar grupo
     */
    suspend fun deleteGroup(group: Group): Boolean {
        return try {
            groupDao.deleteGroup(group)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Verificar se utilizador é membro do grupo
     */
    suspend fun isUserInGroup(groupId: Int, userId: Int): Boolean {
        return groupMemberDao.isUserInGroup(groupId, userId) > 0
    }
    
    /**
     * Obter grupo por ID
     */
    suspend fun getGroupById(groupId: Int): Group? {
        return groupDao.getGroupById(groupId)
    }
}