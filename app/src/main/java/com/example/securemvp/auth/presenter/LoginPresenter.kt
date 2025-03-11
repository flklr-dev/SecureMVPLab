package com.example.securemvp.auth.presenter

import android.content.Context
import com.example.securemvp.auth.contract.AuthContract
import com.example.securemvp.data.model.AuthResult
import com.example.securemvp.data.repository.UserRepository
import com.example.securemvp.utils.InputValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(private val context: Context) : AuthContract.LoginPresenter {
    
    private var view: AuthContract.LoginView? = null
    private val userRepository = UserRepository(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun attachView(view: AuthContract.LoginView) {
        this.view = view
    }
    
    override fun detachView() {
        this.view = null
    }
    
    override fun login(username: String, password: String) {
        // Validate input first
        if (!validateCredentials(username, password)) {
            return
        }
        
        // Sanitize input
        val sanitizedUsername = InputValidator.sanitizeInput(username)
        
        // Show loading state
        view?.showLoading(true)
        
        coroutineScope.launch {
            val result = userRepository.login(sanitizedUsername, password)
            
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                
                when (result) {
                    is AuthResult.Success -> {
                        view?.navigateToMainScreen()
                    }
                    is AuthResult.Error -> {
                        if (result.message.contains("Network")) {
                            view?.showNetworkError(result.message)
                        } else {
                            view?.showLoginError(result.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
    
    override fun validateCredentials(username: String, password: String): Boolean {
        if (username.isBlank()) {
            view?.showLoginError("Username cannot be empty")
            return false
        }
        
        if (password.isBlank()) {
            view?.showLoginError("Password cannot be empty")
            return false
        }
        
        if (!InputValidator.isValidUsername(username)) {
            view?.showLoginError("Invalid username format")
            return false
        }
        
        return true
    }
} 