package com.example.securemvp.utils

import android.text.TextUtils
import android.util.Patterns
import java.util.regex.Pattern

object InputValidator {
    
    // Username validation
    fun isValidUsername(username: String): Boolean {
        // Username should be at least 4 characters and alphanumeric
        val usernamePattern = Pattern.compile("^[a-zA-Z0-9]{4,}$")
        return usernamePattern.matcher(username).matches()
    }
    
    // Email validation
    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Sanitize input to prevent XSS
    fun sanitizeInput(input: String): String {
        return input.replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }
    
    // Validate password match
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
} 