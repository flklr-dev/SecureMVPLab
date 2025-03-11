package com.example.securemvp.utils

import android.text.TextUtils
import android.util.Patterns
import java.util.regex.Pattern

object InputValidator {
    
    // Username validation
    fun isValidUsername(username: String): Boolean {
        // Username should be at least 4 characters and can contain letters, numbers, and underscore
        val usernamePattern = Pattern.compile("^[a-zA-Z0-9_.@]{4,20}$")
        return usernamePattern.matcher(username).matches()
    }
    
    // Email validation
    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Sanitize input to prevent XSS
    fun sanitizeInput(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
            // Add more replacements
            .replace("(", "&#40;")
            .replace(")", "&#41;")
            .replace("{", "&#123;")
            .replace("}", "&#125;")
            .replace("script", "&#115;cript") // Prevent script injection
            .replace("javascript:", "&#106;avascript:") // Prevent javascript: URLs
            .trim()
    }
    
    // Validate password match
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    // Add validation for specific input types
    fun sanitizeHtmlContent(content: String): String {
        // Remove all HTML tags
        return content.replace(Regex("<[^>]*>"), "")
    }

    // Add method to validate against common XSS patterns
    fun containsXSSPatterns(input: String): Boolean {
        val xssPatterns = listOf(
            "<script[^>]*>.*?</script>",
            "javascript:",
            "onload=",
            "onerror=",
            "onclick=",
            "alert\\s*\\(",
            "eval\\s*\\("
        )
        return xssPatterns.any { pattern ->
            input.lowercase().contains(Regex(pattern))
        }
    }
} 