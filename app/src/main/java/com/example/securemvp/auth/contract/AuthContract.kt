package com.example.securemvp.auth.contract

interface AuthContract {
    
    interface LoginView {
        fun showLoading(isLoading: Boolean)
        fun showLoginError(message: String)
        fun showNetworkError(message: String)
        fun navigateToMainScreen()
        fun showLoginSuccess()
    }
    
    interface LoginPresenter {
        fun attachView(view: LoginView)
        fun detachView()
        fun login(email: String, password: String)
        fun validateCredentials(email: String, password: String): Boolean
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
        fun register(email: String, password: String, confirmPassword: String)
        fun validateRegistrationInput(email: String, password: String, confirmPassword: String): Boolean
        fun checkPasswordStrength(password: String)
    }
} 