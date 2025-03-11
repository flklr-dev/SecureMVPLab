package com.example.securemvp.data.source.remote

import android.content.Context
import com.example.securemvp.data.source.local.SecureSharedPreferences
import okhttp3.OkHttpClient
import okhttp3.CertificatePinner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("your.domain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                    .build()
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun createApiService(context: Context): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://your.api.url/")
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        return retrofit.create(ApiService::class.java)
    }
}

class MockApiService(private val securePrefs: SecureSharedPreferences) : ApiService {
    override suspend fun login(request: ApiService.LoginRequest): retrofit2.Response<ApiService.LoginResponse> {
        // Simulate successful login
        return retrofit2.Response.success(
            ApiService.LoginResponse(
                success = true,
                message = "Login successful",
                user = null  // The local storage will handle user data
            )
        )
    }
    
    override suspend fun register(request: ApiService.RegisterRequest): retrofit2.Response<ApiService.RegisterResponse> {
        // Check for existing email
        if (securePrefs.isUserRegistered(request.email)) {
            return retrofit2.Response.success(
                ApiService.RegisterResponse(
                    success = false,
                    message = "Email already registered"
                )
            )
        }
        
        return retrofit2.Response.success(
            ApiService.RegisterResponse(
                success = true,
                message = "Registration successful"
            )
        )
    }
} 