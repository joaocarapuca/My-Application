package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.User
import com.example.myapplication.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para funcionalidades de administração
 */
class AdminViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: UserRepository
    
    // Lista de utilizadores
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Mensagens de sucesso/erro
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database)
        loadUsers()
    }
    
    /**
     * Carregar lista de utilizadores
     */
    private fun loadUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { userList ->
                _users.value = userList
            }
        }
    }
    
    /**
     * Criar novo utilizador
     */
    fun createUser(name: String, email: String, password: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            
            try {
                val success = repository.registerUser(name, email, password, isAdmin)
                if (success) {
                    _message.value = "Utilizador criado com sucesso!"
                } else {
                    _message.value = "Erro: Email já existe"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao criar utilizador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Alterar senha de utilizador
     */
    fun changeUserPassword(userId: Int, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            
            try {
                val success = repository.updatePassword(userId, newPassword)
                if (success) {
                    _message.value = "Senha alterada com sucesso!"
                } else {
                    _message.value = "Erro ao alterar senha"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao alterar senha: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Eliminar utilizador
     */
    fun deleteUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            
            try {
                val success = repository.deleteUser(user)
                if (success) {
                    _message.value = "Utilizador eliminado com sucesso!"
                } else {
                    _message.value = "Erro ao eliminar utilizador"
                }
            } catch (e: Exception) {
                _message.value = "Erro ao eliminar utilizador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpar mensagem
     */
    fun clearMessage() {
        _message.value = null
    }
}