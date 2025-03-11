package com.example.securemvp.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64
import java.util.regex.Pattern

object SecurityUtils {
    
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val ACCOUNT_TYPE = "com.example.securemvp"
    
    // Password strength regex patterns
    private val PASSWORD_PATTERN = Pattern.compile(
        "^" +
        "(?=.*[0-9])" +         // at least 1 digit
        "(?=.*[a-z])" +         // at least 1 lower case letter
        "(?=.*[A-Z])" +         // at least 1 upper case letter
        "(?=.*[!@#$%^&*()_+])" + // at least 1 special character
        "(?=\\S+$)" +           // no white spaces
        ".{8,}" +               // at least 8 characters
        "$"
    )
    
    // Account Manager Integration
    fun addAccount(context: Context, email: String, password: String, salt: String): Boolean {
        try {
            val accountManager = AccountManager.get(context)
            val account = Account(email, ACCOUNT_TYPE)
            
            // Add the account
            val success = accountManager.addAccountExplicitly(account, password, Bundle().apply {
                putString("salt", salt)
            })
            
            if (success) {
                // Store additional user data if needed
                accountManager.setUserData(account, "created_at", System.currentTimeMillis().toString())
                accountManager.setUserData(account, "last_login", System.currentTimeMillis().toString())
            }
            
            return success
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getAccount(context: Context, email: String): Account? {
        val accountManager = AccountManager.get(context)
        return accountManager.getAccountsByType(ACCOUNT_TYPE)
            .firstOrNull { it.name == email }
    }

    fun removeAccount(context: Context, email: String) {
        val accountManager = AccountManager.get(context)
        getAccount(context, email)?.let { account ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccount(account, null, null, null)
            } else {
                @Suppress("DEPRECATION")
                accountManager.removeAccount(account, null, null)
            }
        }
    }

    fun updatePassword(context: Context, email: String, newPassword: String) {
        val accountManager = AccountManager.get(context)
        getAccount(context, email)?.let { account ->
            accountManager.setPassword(account, newPassword)
            accountManager.setUserData(account, "last_password_change", System.currentTimeMillis().toString())
        }
    }

    fun getAccountSalt(context: Context, email: String): String? {
        val accountManager = AccountManager.get(context)
        return getAccount(context, email)?.let { account ->
            accountManager.getUserData(account, "salt")
        }
    }
    
    // Generate a random salt
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }
    
    // Hash password with PBKDF2
    fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }
    
    // Verify password
    fun verifyPassword(password: String, salt: ByteArray, storedHash: String): Boolean {
        val calculatedHash = hashPassword(password, salt)
        return calculatedHash == storedHash
    }
    
    // Check password strength
    fun isPasswordStrong(password: String): Boolean {
        return PASSWORD_PATTERN.matcher(password).matches()
    }
    
    // Get password strength feedback
    fun getPasswordStrengthFeedback(password: String): String {
        val feedback = StringBuilder()
        
        if (password.length < 8) {
            feedback.append("Password must be at least 8 characters long\n")
        }
        if (!password.any { it.isDigit() }) {
            feedback.append("Password must contain at least one digit\n")
        }
        if (!password.any { it.isLowerCase() }) {
            feedback.append("Password must contain at least one lowercase letter\n")
        }
        if (!password.any { it.isUpperCase() }) {
            feedback.append("Password must contain at least one uppercase letter\n")
        }
        if (!password.any { "!@#$%^&*()_+".contains(it) }) {
            feedback.append("Password must contain at least one special character\n")
        }
        if (password.contains(" ")) {
            feedback.append("Password must not contain spaces\n")
        }
        
        return feedback.toString().trim()
    }
    
    // Create or get encrypted shared preferences
    fun getEncryptedSharedPreferences(context: Context, fileName: String): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
} 