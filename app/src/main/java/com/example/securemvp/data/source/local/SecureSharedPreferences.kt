package com.example.securemvp.data.source.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.securemvp.utils.SecurityUtils
import java.util.UUID

class SecureSharedPreferences(private val context: Context) {
    
    // Add these constants
    companion object {
        private const val EMAIL_PREFIX = "email_"
        private const val PASSWORD_HASH_PREFIX = "password_hash_"
        private const val SALT_PREFIX = "salt_"
        private const val CURRENT_USER_EMAIL = "current_user_email"
    }
    
    private val securePrefs by lazy {
        SecurityUtils.getEncryptedSharedPreferences(context, "secure_user_prefs")
    }
    
    fun saveUserCredentials(email: String, passwordHash: String, salt: String) {
        val editor = securePrefs.edit()
        
        // Store user credentials
        editor.putString("$EMAIL_PREFIX$email", email)
        editor.putString("$PASSWORD_HASH_PREFIX$email", passwordHash)
        editor.putString("$SALT_PREFIX$email", salt)
        
        // Set as current user
        editor.putString(CURRENT_USER_EMAIL, email)
        
        editor.apply()
    }
    
    fun getUserSalt(email: String): String? {
        return securePrefs.getString("$SALT_PREFIX$email", null)
    }
    
    fun getUserPasswordHash(email: String): String? {
        return securePrefs.getString("$PASSWORD_HASH_PREFIX$email", null)
    }
    
    fun isUserRegistered(email: String): Boolean {
        return securePrefs.contains("$EMAIL_PREFIX$email")
    }
    
    fun logout() {
        val editor = securePrefs.edit()
        editor.remove(CURRENT_USER_EMAIL)
        editor.apply()
    }
    
    fun clearAllUserData() {
        val editor = securePrefs.edit()
        editor.clear()
        editor.apply()
    }
    
    fun getCurrentEmail(): String? {
        return securePrefs.getString(CURRENT_USER_EMAIL, null)
    }
    
    fun setCurrentUser(email: String) {
        val editor = securePrefs.edit()
        editor.putString(CURRENT_USER_EMAIL, email)
        editor.apply()
    }
} 