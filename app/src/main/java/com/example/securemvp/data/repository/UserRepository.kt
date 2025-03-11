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
    
    suspend fun login(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user exists
                if (!securePrefs.isUserRegistered(email)) {
                    return@withContext AuthResult.Error("Account not found. Please register first.")
                }

                // Get stored password hash and salt
                val storedSalt = securePrefs.getUserSalt(email) ?: return@withContext AuthResult.Error(
                    "Invalid credentials. Please try again."
                )
                val storedHash = securePrefs.getUserPasswordHash(email) ?: return@withContext AuthResult.Error(
                    "Invalid credentials. Please try again."
                )

                // Hash the provided password with stored salt
                val salt = Base64.getDecoder().decode(storedSalt)
                val passwordHash = SecurityUtils.hashPassword(password, salt)

                // Compare password hashes
                if (passwordHash != storedHash) {
                    return@withContext AuthResult.Error("Incorrect password. Please try again.")
                }
                
                // Set current user
                securePrefs.setCurrentUser(email)

                return@withContext AuthResult.Success(
                    User(
                        id = "local_user",
                        email = email,
                        passwordHash = passwordHash,
                        salt = storedSalt
                    )
                )
            } catch (e: Exception) {
                return@withContext AuthResult.Error("Network error: Please check your connection.")
            }
        }
    }
    
    suspend fun register(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user already exists locally
                if (securePrefs.isUserRegistered(email)) {
                    return@withContext AuthResult.Error("User already exists")
                }
                
                // Try to register with remote API
                val response = apiService.register(ApiService.RegisterRequest(email, password))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Save user credentials locally
                    val salt = SecurityUtils.generateSalt()
                    val saltString = Base64.getEncoder().encodeToString(salt)
                    val passwordHash = SecurityUtils.hashPassword(password, salt)
                    
                    securePrefs.saveUserCredentials(
                        email = email,
                        passwordHash = passwordHash,
                        salt = saltString
                    )
                    
                    return@withContext AuthResult.Success(
                        User(
                            id = "local_user",
                            email = email,
                            passwordHash = passwordHash,
                            salt = saltString
                        )
                    )
                }
                
                return@withContext AuthResult.Error(response.body()?.message ?: "Registration failed")
            } catch (e: Exception) {
                return@withContext AuthResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    fun logout() {
        securePrefs.logout()
    }


} 
