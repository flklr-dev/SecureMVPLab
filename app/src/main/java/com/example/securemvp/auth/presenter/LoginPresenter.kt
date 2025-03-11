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
    private var isLoginInProgress = false
    
    override fun attachView(view: AuthContract.LoginView) {
        this.view = view
    }
    
    override fun detachView() {
        isLoginInProgress = false
        this.view = null
    }
    
    override fun login(email: String, password: String) {
        if (isLoginInProgress) {
            return
        }
        
        if (!validateCredentials(email, password)) {
            view?.showLoading(false)
            return
        }
        
        isLoginInProgress = true
        val sanitizedEmail = InputValidator.sanitizeInput(email)
        view?.showLoading(true)
        
        coroutineScope.launch {
            try {
                val result = userRepository.login(sanitizedEmail, password)
                
                withContext(Dispatchers.Main) {
                    isLoginInProgress = false
                    
                    when (result) {
                        is AuthResult.Success -> {
                            view?.showLoginSuccess()
                        }
                        is AuthResult.Error -> {
                            view?.showLoading(false)
                            when {
                                result.message.contains("Account not found") -> 
                                    view?.showLoginError("This account doesn't exist. Register an account first.")
                                result.message.contains("Incorrect password") -> 
                                    view?.showLoginError("Incorrect password. Please try again.")
                                result.message.contains("Invalid credentials") -> 
                                    view?.showLoginError("Invalid email or password. Please check your credentials.")
                                result.message.contains("Network error") -> 
                                    view?.showLoginError("Unable to connect. Please check your internet connection.")
                                else -> view?.showLoginError(result.message)
                            }
                        }
                        else -> {
                            view?.showLoading(false)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoginInProgress = false
                    view?.showLoading(false)
                    view?.showLoginError("An unexpected error occurred. Please try again.")
                }
            }
        }
    }
    
    override fun validateCredentials(email: String, password: String): Boolean {
        if (email.isBlank()) {
            view?.showLoginError("Email is required")
            return false
        }
        
        if (!InputValidator.isValidEmail(email)) {
            view?.showLoginError("Please enter a valid email address")
            return false
        }
        
        if (password.isBlank()) {
            view?.showLoginError("Password is required")
            return false
        }

        if (password.length < 8) {
            view?.showLoginError("Password must be at least 8 characters long")
            return false
        }
        
        return true
    }
} 