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
 * ViewModel para gerir autenticação de utilizadores
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: UserRepository
    
    // Estado do utilizador atual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Mensagens de erro
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database)
    }
    
    /**
     * Fazer login
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val user = repository.loginUser(email, password)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    _errorMessage.value = "Email ou senha incorretos"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao fazer login: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Registar novo utilizador
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val success = repository.registerUser(name, email, password)
                if (success) {
                    // Fazer login automático após registo
                    login(email, password)
                } else {
                    _errorMessage.value = "Email já existe ou erro no registo"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao registar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Fazer logout
     */
    fun logout() {
        _currentUser.value = null
        _errorMessage.value = null
    }
    
    /**
     * Limpar mensagem de erro
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Verificar se utilizador é admin
     */
    fun isAdmin(): Boolean {
        return _currentUser.value?.isAdmin == true
    }
}