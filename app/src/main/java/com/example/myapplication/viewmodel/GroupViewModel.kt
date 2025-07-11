package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.*
import com.example.myapplication.repository.GroupRepository
import com.example.myapplication.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerir grupos
 */
class GroupViewModel(application: Application) : AndroidViewModel(application) {
    
    private val groupRepository: GroupRepository
    private val userRepository: UserRepository
    
    // Lista de grupos
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups
    
    // Grupos do utilizador
    private val _userGroups = MutableStateFlow<List<Group>>(emptyList())
    val userGroups: StateFlow<List<Group>> = _userGroups
    
    // Membros do grupo selecionado
    private val _groupMembers = MutableStateFlow<List<User>>(emptyList())
    val groupMembers: StateFlow<List<User>> = _groupMembers
    
    // Mensagens do grupo
    private val _groupMessages = MutableStateFlow<List<GroupMessageWithSender>>(emptyList())
    val groupMessages: StateFlow<List<GroupMessageWithSender>> = _groupMessages
    
    // Utilizadores disponíveis para adicionar
    private val _availableUsers = MutableStateFlow<List<User>>(emptyList())
    val availableUsers: StateFlow<List<User>> = _availableUsers
    
    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Mensagens
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    
    // Grupo atual
    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup
    
    init {
        val database = AppDatabase.getDatabase(application)
        groupRepository = GroupRepository(database)
        userRepository = UserRepository(database)
        loadAllGroups()
        loadAllUsers()
    }
    
    /**
     * Carregar todos os grupos
     */
    private fun loadAllGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groupList ->
                _groups.value = groupList
            }
        }
    }
    
    /**
     * Carregar todos os utilizadores
     */
    private fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { userList ->
                _availableUsers.value = userList
            }
        }
    }
    
    /**
     * Carregar grupos do utilizador
     */
    fun loadUserGroups(userId: Int) {
        viewModelScope.launch {
            groupRepository.getUserGroups(userId).collect { groupList ->
                _userGroups.value = groupList
            }
        }
    }
    
    /**
     * Criar novo grupo
     */
    fun createGroup(name: String, description: String, createdBy: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            
            try {
                val groupId = groupRepository.createGroup(name, description, createdBy)
                if (groupId > 0) {
                    _message.value = "Grupo '$name' criado com sucesso!"
                } else {
                    _message.value = "Erro ao criar grupo"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao criar grupo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Adicionar membro ao grupo
     */
    fun addMemberToGroup(groupId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val success = groupRepository.addMemberToGroup(groupId, userId)
                if (success) {
                    _message.value = "Membro adicionado com sucesso!"
                    loadGroupMembers(groupId) // Recarregar membros
                } else {
                    _message.value = "Utilizador já é membro do grupo"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao adicionar membro: ${e.message}"
            }
        }
    }
    
    /**
     * Remover membro do grupo
     */
    fun removeMemberFromGroup(groupId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val success = groupRepository.removeMemberFromGroup(groupId, userId)
                if (success) {
                    _message.value = "Membro removido com sucesso!"
                    loadGroupMembers(groupId) // Recarregar membros
                } else {
                    _message.value = "Erro ao remover membro"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao remover membro: ${e.message}"
            }
        }
    }
    
    /**
     * Carregar membros do grupo
     */
    fun loadGroupMembers(groupId: Int) {
        viewModelScope.launch {
            groupRepository.getGroupMembers(groupId).collect { memberList ->
                _groupMembers.value = memberList
            }
        }
    }
    
    /**
     * Carregar mensagens do grupo
     */
    fun loadGroupMessages(groupId: Int) {
        viewModelScope.launch {
            groupRepository.getGroupMessages(groupId).collect { messageList ->
                _groupMessages.value = messageList
            }
        }
    }
    
    /**
     * Enviar mensagem no grupo
     */
    fun sendGroupMessage(groupId: Int, senderId: Int, content: String) {
        viewModelScope.launch {
            try {
                val success = groupRepository.sendGroupMessage(groupId, senderId, content)
                if (!success) {
                    _message.value = "Erro ao enviar mensagem"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao enviar mensagem: ${e.message}"
            }
        }
    }
    
    /**
     * Eliminar grupo
     */
    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            
            try {
                val success = groupRepository.deleteGroup(group)
                if (success) {
                    _message.value = "Grupo '${group.name}' eliminado com sucesso!"
                } else {
                    _message.value = "Erro ao eliminar grupo"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao eliminar grupo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Definir grupo atual
     */
    fun setCurrentGroup(group: Group) {
        _currentGroup.value = group
        loadGroupMembers(group.id)
        loadGroupMessages(group.id)
    }
    
    /**
     * Limpar grupo atual
     */
    fun clearCurrentGroup() {
        _currentGroup.value = null
        _groupMembers.value = emptyList()
        _groupMessages.value = emptyList()
    }
    
    /**
     * Limpar mensagem
     */
    fun clearMessage() {
        _message.value = null
    }
}