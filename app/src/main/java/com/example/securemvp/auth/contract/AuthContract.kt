package com.example.securemvp.auth.contract

interface AuthContract {
    
    interface LoginView {
        fun showLoading(isLoading: Boolean)
        fun showLoginError(message: String)
        fun navigateToMainScreen()
        fun showNetworkError(message: String)
    }
    
    interface LoginPresenter {
        fun attachView(view: LoginView)
        fun detachView()
        fun login(username: String, password: String)
        fun validateCredentials(username: String, password: String): Boolean
    }
    
    interface RegistrationView {
        fun showLoading(isLoading: Boolean)
        fun showRegistrationError(message: String)
        fun showRegistrationSuccess()
        fun showPasswordStrengthFeedback(isStrong: Boolean, message: String)
    }
    
    interface RegistrationPresenter {
        fun attachView(view: RegistrationView)
        fun detachView()
        fun register(username: String, password: String, confirmPassword: String)
        fun validateRegistrationInput(username: String, password: String, confirmPassword: String): Boolean
        fun checkPasswordStrength(password: String)
    }
} 