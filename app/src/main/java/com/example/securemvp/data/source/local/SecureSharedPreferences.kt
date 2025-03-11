package com.example.securemvp.data.source.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.securemvp.utils.SecurityUtils
import java.util.UUID

class SecureSharedPreferences(private val context: Context) {
    
    private val securePrefs by lazy {
        SecurityUtils.getEncryptedSharedPreferences(context, "secure_user_prefs")
    }
    
    fun saveUserCredentials(username: String, passwordHash: String, salt: String) {
        val userId = UUID.randomUUID().toString()
        securePrefs.edit()
            .putString("user_id", userId)
            .putString("username", username)
            .putString("password_hash", passwordHash)
            .putString("salt", salt)
            .putLong("created_at", System.currentTimeMillis())
            .apply()
    }
    
    fun getUserSalt(username: String): String? {
        return if (securePrefs.getString("username", "") == username) {
            securePrefs.getString("salt", null)
        } else {
            null
        }
    }
    
    fun getUserPasswordHash(username: String): String? {
        return if (securePrefs.getString("username", "") == username) {
            securePrefs.getString("password_hash", null)
        } else {
            null
        }
    }
    
    fun isUserRegistered(username: String): Boolean {
        return securePrefs.getString("username", "") == username
    }
    
    fun clearUserData() {
        securePrefs.edit().clear().apply()
    }
} 