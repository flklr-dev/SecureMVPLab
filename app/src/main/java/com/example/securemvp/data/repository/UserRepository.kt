package com.example.securemvp.data.repository

import android.content.Context
import com.example.securemvp.data.model.AuthResult
import com.example.securemvp.data.model.User
import com.example.securemvp.data.source.local.SecureSharedPreferences
import com.example.securemvp.data.source.remote.ApiClient
import com.example.securemvp.data.source.remote.ApiService
import com.example.securemvp.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

class UserRepository(private val context: Context) {
    
    private val securePrefs = SecureSharedPreferences(context)
    private val apiService = ApiClient.createApiService(context)
    
    suspend fun login(username: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // First try local authentication
                if (securePrefs.isUserRegistered(username)) {
                    val saltString = securePrefs.getUserSalt(username)
                    val storedHash = securePrefs.getUserPasswordHash(username)
                    
                    if (saltString != null && storedHash != null) {
                        val salt = Base64.getDecoder().decode(saltString)
                        val isValid = SecurityUtils.verifyPassword(password, salt, storedHash)
                        
                        if (isValid) {
                            // Local authentication successful
                            return@withContext AuthResult.Success(
                                User(
                                    id = "local_user",
                                    username = username,
                                    passwordHash = storedHash,
                                    salt = saltString
                                )
                            )
                        }
                    }
                }
                
                // If local authentication fails or user not found locally, try remote
                val response = apiService.login(ApiService.LoginRequest(username, password))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    if (user != null) {
                        // Save user credentials locally for future offline authentication
                        val salt = SecurityUtils.generateSalt()
                        val saltString = Base64.getEncoder().encodeToString(salt)
                        val passwordHash = SecurityUtils.hashPassword(password, salt)
                        
                        securePrefs.saveUserCredentials(
                            username = username,
                            passwordHash = passwordHash,
                            salt = saltString
                        )
                        
                        return@withContext AuthResult.Success(user)
                    }
                }
                
                return@withContext AuthResult.Error(response.body()?.message ?: "Authentication failed")
            } catch (e: Exception) {
                return@withContext AuthResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    suspend fun register(username: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user already exists locally
                if (securePrefs.isUserRegistered(username)) {
                    return@withContext AuthResult.Error("User already exists")
                }
                
                // Try to register with remote API
                val response = apiService.register(ApiService.RegisterRequest(username, password))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Save user credentials locally
                    val salt = SecurityUtils.generateSalt()
                    val saltString = Base64.getEncoder().encodeToString(salt)
                    val passwordHash = SecurityUtils.hashPassword(password, salt)
                    
                    securePrefs.saveUserCredentials(
                        username = username,
                        passwordHash = passwordHash,
                        salt = saltString
                    )
                    
                    return@withContext AuthResult.Success(
                        User(
                            id = "local_user",
                            username = username,
                            passwordHash = passwordHash,
                            salt = saltString
                        )
                    )
                }
                
                return@withContext AuthResult.Error(response.body()?.message ?: "Registration failed")
            } catch (e: Exception) {
                // If network is unavailable, register locally only
                val salt = SecurityUtils.generateSalt()
                val saltString = Base64.getEncoder().encodeToString(salt)
                val passwordHash = SecurityUtils.hashPassword(password, salt)
                
                securePrefs.saveUserCredentials(
                    username = username,
                    passwordHash = passwordHash,
                    salt = saltString
                )
                
                return@withContext AuthResult.Success(
                    User(
                        id = "local_user",
                        username = username,
                        passwordHash = passwordHash,
                        salt = saltString
                    )
                )
            }
        }
    }
    
    fun logout() {
        securePrefs.clearUserData()
    }
} 