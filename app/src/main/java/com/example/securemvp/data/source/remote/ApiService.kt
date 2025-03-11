package com.example.securemvp.data.source.remote

import com.example.securemvp.data.model.AuthResult
import com.example.securemvp.data.model.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): retrofit2.Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): retrofit2.Response<RegisterResponse>
    
    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(val success: Boolean, val message: String, val user: User?)
    
    data class RegisterRequest(
        val email: String,
        val password: String
    )
    data class RegisterResponse(val success: Boolean, val message: String)
} 