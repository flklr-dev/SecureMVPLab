package com.example.securemvp.auth.presenter

import android.content.Context
import com.example.securemvp.auth.contract.AuthContract
import com.example.securemvp.data.model.AuthResult
import com.example.securemvp.data.repository.UserRepository
import com.example.securemvp.utils.InputValidator
import com.example.securemvp.utils.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationPresenter(private val context: Context) : AuthContract.RegistrationPresenter {
    
    private var view: AuthContract.RegistrationView? = null
    private val userRepository = UserRepository(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun attachView(view: AuthContract.RegistrationView) {
        this.view = view
    }
    
    override fun detachView() {
        this.view = null
    }
    
    override fun register(username: String, password: String, confirmPassword: String) {
        // Validate input first
        if (!validateRegistrationInput(username, password, confirmPassword)) {
            return
        }
        
        // Sanitize input
        val sanitizedUsername = InputValidator.sanitizeInput(username)
        
        // Show loading state
        view?.showLoading(true)
        
        coroutineScope.launch {
            val result = userRepository.register(sanitizedUsername, password)
            
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                
                when (result) {
                    is AuthResult.Success -> {
                        view?.showRegistrationSuccess()
                    }
                    is AuthResult.Error -> {
                        view?.showRegistrationError(result.message)
                    }
                    else -> {}
                }
            }
        }
    }
    
    override fun validateRegistrationInput(
        username: String, 
        password: String, 
        confirmPassword: String
    ): Boolean {
        if (username.isBlank()) {
            view?.showRegistrationError("Username cannot be empty")
            return false
        }
        
        if (!InputValidator.isValidUsername(username)) {
            view?.showRegistrationError("Username must be at least 4 characters and alphanumeric")
            return false
        }
        
        if (password.isBlank()) {
            view?.showRegistrationError("Password cannot be empty")
            return false
        }
        
        if (!SecurityUtils.isPasswordStrong(password)) {
            val feedback = SecurityUtils.getPasswordStrengthFeedback(password)
            view?.showPasswordStrengthFeedback(false, feedback)
            return false
        }
        
        if (!InputValidator.doPasswordsMatch(password, confirmPassword)) {
            view?.showRegistrationError("Passwords do not match")
            return false
        }
        
        return true
    }
    
    override fun checkPasswordStrength(password: String) {
        if (password.isBlank()) {
            view?.showPasswordStrengthFeedback(false, "Password cannot be empty")
            return
        }
        
        val isStrong = SecurityUtils.isPasswordStrong(password)
        val feedback = if (isStrong) {
            "Password is strong"
        } else {
            SecurityUtils.getPasswordStrengthFeedback(password)
        }
        
        view?.showPasswordStrengthFeedback(isStrong, feedback)
    }
} 