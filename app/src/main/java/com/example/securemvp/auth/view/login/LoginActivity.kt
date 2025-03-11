package com.example.securemvp.auth.view.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.securemvp.MainActivity
import com.example.securemvp.auth.contract.AuthContract
import com.example.securemvp.auth.presenter.LoginPresenter
import com.example.securemvp.auth.view.registration.RegistrationActivity
import com.example.securemvp.ui.theme.SecureMVPLabTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.securemvp.ui.components.PasswordField
import androidx.compose.ui.focus.FocusState
import com.example.securemvp.utils.InputValidator
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.AlertDialog

class LoginActivity : ComponentActivity(), AuthContract.LoginView {
    
    private lateinit var presenter: LoginPresenter
    private val snackbarHostState = SnackbarHostState()
    private var isLoginInProgress = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        presenter = LoginPresenter(this)
        presenter.attachView(this)
        
        setContent {
            SecureMVPLabTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        if (!isLoginInProgress) {
                            isLoginInProgress = true
                            presenter.login(email, password)
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegistrationActivity::class.java))
                    },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
    
    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
    
    override fun showLoading(isLoading: Boolean) {
        isLoginInProgress = isLoading
        setContent {
            SecureMVPLabTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        if (!isLoginInProgress) {
                            isLoginInProgress = true
                            presenter.login(email, password)
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegistrationActivity::class.java))
                    },
                    snackbarHostState = snackbarHostState,
                    isLoading = isLoading
                )
            }
        }
    }
    
    override fun showLoginError(message: String) {
        isLoginInProgress = false
        lifecycleScope.launch {
            setContent {
                SecureMVPLabTheme {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            if (!isLoginInProgress) {
                                isLoginInProgress = true
                                presenter.login(email, password)
                            }
                        },
                        onRegisterClick = {
                            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
                        },
                        snackbarHostState = snackbarHostState,
                        errorMessage = message
                    )
                }
            }
        }
    }
    
    override fun showNetworkError(message: String) {
        showLoginError("Network error: $message")
    }
    
    override fun showLoginSuccess() {
        isLoginInProgress = false
        lifecycleScope.launch {
            setContent {
                SecureMVPLabTheme {
                    LoginScreen(
                        onLoginClick = { _, _ -> },
                        onRegisterClick = { },
                        snackbarHostState = snackbarHostState,
                        showSuccessDialog = true
                    )
                }
            }
            
            // Navigate to main screen after delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing) {
                    navigateToMainScreen()
                }
            }, 2000)
        }
    }
    
    override fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    showSuccessDialog: Boolean = false,
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localIsLoading by remember { mutableStateOf(isLoading) }
    var currentErrorMessage by remember { mutableStateOf(errorMessage) }
    var showSuccessDialogState by remember { mutableStateOf(showSuccessDialog) }
    
    // Add state for field validation
    var hasEmailFocused by remember { mutableStateOf(false) }
    var hasPasswordFocused by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Update local loading state when prop changes
    LaunchedEffect(isLoading) {
        localIsLoading = isLoading
    }
    
    // Update error message when prop changes
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            currentErrorMessage = errorMessage
            localIsLoading = false
        }
    }
    
    // Update success dialog state when prop changes
    LaunchedEffect(showSuccessDialog) {
        showSuccessDialogState = showSuccessDialog
        if (showSuccessDialog) {
            localIsLoading = false
        }
    }

    // Show error dialog
    currentErrorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { currentErrorMessage = null },
            title = { Text("Login Failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { 
                    currentErrorMessage = null
                    localIsLoading = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Show success dialog
    if (showSuccessDialogState) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Welcome Back!") },
            text = { Text("Login successful! Taking you to your dashboard...") },
            confirmButton = { }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = validateEmail(it)
                },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasEmailFocused = true
                        }
                        if (hasEmailFocused && !focusState.isFocused) {
                            emailError = validateEmail(email)
                        }
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                enabled = !localIsLoading
            )
            
            emailError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Password field
            PasswordField(
                value = password,
                onValueChange = { 
                    password = it
                    if (hasPasswordFocused) {
                        passwordError = validatePassword(it)
                    }
                },
                label = "Password",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasPasswordFocused = true
                        }
                        if (hasPasswordFocused && !focusState.isFocused) {
                            passwordError = validatePassword(password)
                        }
                    },
                enabled = !localIsLoading
            )
            
            passwordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (!localIsLoading) {
                        localIsLoading = true
                        onLoginClick(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !localIsLoading && emailError == null && passwordError == null &&
                        email.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (localIsLoading) "Logging in..." else "Login")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onRegisterClick,
                enabled = !localIsLoading
            ) {
                Text("Don't have an account? Register")
            }
        }
    }
}

private fun validateEmail(email: String): String? {
    return when {
        email.isBlank() -> "Email is required"
        !InputValidator.isValidEmail(email) -> "Please enter a valid email address"
        else -> null
    }
}

private fun validatePassword(password: String): String? {
    return when {
        password.isBlank() -> "Password is required"
        else -> null
    }
} 