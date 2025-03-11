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

class RegistrationPresenter(private val userRepository: UserRepository) : AuthContract.RegistrationPresenter {
    
    private var view: AuthContract.RegistrationView? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    
    override fun attachView(view: AuthContract.RegistrationView) {
        this.view = view
    }
    
    override fun detachView() {
        this.view = null
    }
    
    override fun register(email: String, password: String, confirmPassword: String) {
        // Validate input first
        if (!validateRegistrationInput(email, password, confirmPassword)) {
            return
        }
        
        // Sanitize input
        val sanitizedEmail = InputValidator.sanitizeInput(email)
        
        // Show loading state
        view?.showLoading(true)
        
        coroutineScope.launch {
            val result = userRepository.register(sanitizedEmail, password)
            
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
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (email.isBlank()) {
            view?.showRegistrationError("Email is required")
            return false
        }
        if (!InputValidator.isValidEmail(email)) {
            view?.showRegistrationError("Please enter a valid email address")
            return false
        }
        if (password.isBlank()) {
            view?.showRegistrationError("Password is required")
            return false
        }
        if (confirmPassword != password) {
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