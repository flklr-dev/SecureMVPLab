package com.example.securemvp.auth.view.registration

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.securemvp.auth.contract.AuthContract
import com.example.securemvp.auth.presenter.RegistrationPresenter
import com.example.securemvp.ui.theme.SecureMVPLabTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.securemvp.utils.InputValidator
import com.example.securemvp.ui.components.PasswordField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusState
import com.example.securemvp.data.repository.UserRepository
import androidx.compose.runtime.rememberCoroutineScope

class RegistrationActivity : ComponentActivity(), AuthContract.RegistrationView {
    
    private lateinit var presenter: RegistrationPresenter
    private val snackbarHostState = SnackbarHostState()
    private val userRepository by lazy { UserRepository(applicationContext) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        presenter = RegistrationPresenter(userRepository)
        presenter.attachView(this)
        
        setContent {
            SecureMVPLabTheme {
                RegistrationScreen(
                    onRegisterClick = { email, password, confirmPassword ->
                        presenter.register(email, password, confirmPassword)
                    },
                    onPasswordChange = { password ->
                        presenter.checkPasswordStrength(password)
                    },
                    onBackClick = { finish() },
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
        // Update loading state in the UI
        runOnUiThread {
            // The loading state is handled in the Composable
        }
    }
    
    override fun showRegistrationError(message: String) {
        lifecycleScope.launch {
            // Show error dialog
            setContent {
                SecureMVPLabTheme {
                    RegistrationScreen(
                        onRegisterClick = { email, password, confirmPassword ->
                            presenter.register(email, password, confirmPassword)
                        },
                        onPasswordChange = { password ->
                            presenter.checkPasswordStrength(password)
                        },
                        onBackClick = { finish() },
                        snackbarHostState = snackbarHostState,
                        errorMessage = message
                    )
                }
            }
        }
    }
    
    override fun showRegistrationSuccess() {
        lifecycleScope.launch {
            // Show success dialog
            setContent {
                SecureMVPLabTheme {
                    RegistrationScreen(
                        onRegisterClick = { _, _, _ -> },
                        onPasswordChange = { },
                        onBackClick = { },
                        snackbarHostState = snackbarHostState,
                        showSuccessDialog = true
                    )
                }
            }
            
            // Navigate back to login after delay
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 2000)
        }
    }
    
    override fun showPasswordStrengthFeedback(isStrong: Boolean, message: String) {
        // Update password strength indicator in the UI
        runOnUiThread {
            // The password strength feedback is handled in the Composable
        }
    }
}

@Composable
fun RegistrationScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    showSuccessDialog: Boolean = false,
    errorMessage: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    
    var currentErrorMessage by remember { mutableStateOf(errorMessage) }
    
    // Update error message when prop changes
    LaunchedEffect(errorMessage) {
        currentErrorMessage = errorMessage
    }

    // Show error dialog
    currentErrorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { currentErrorMessage = null },
            title = { Text("Registration Failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { currentErrorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    var hasEmailFocused by remember { mutableStateOf(false) }
    var hasPasswordFocused by remember { mutableStateOf(false) }
    var hasConfirmPasswordFocused by remember { mutableStateOf(false) }
    
    var passwordComplexity by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }

    val passwordRequirements = listOf(
        "At least 8 characters" to { pwd: String -> pwd.length >= 8 },
        "At least one digit" to { pwd: String -> pwd.any { it.isDigit() } },
        "At least one lowercase letter" to { pwd: String -> pwd.any { it.isLowerCase() } },
        "At least one uppercase letter" to { pwd: String -> pwd.any { it.isUpperCase() } },
        "At least one special character" to { pwd: String -> pwd.any { "!@#$%^&*()_+".contains(it) } }
    )

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Success!") },
            text = { Text("Registration successful! Redirecting to login...") },
            confirmButton = { }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (hasEmailFocused) {
                        emailError = validateEmail(it)
                    }
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasEmailFocused = true
                        } else if (hasEmailFocused && email.isBlank()) {
                            emailError = "Email is required"
                        }
                    },
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = password,
                onValueChange = { newPassword -> 
                    password = newPassword
                    if (hasPasswordFocused) {
                        passwordComplexity = passwordRequirements.map { (requirement, validator) ->
                            requirement to validator(newPassword)
                        }
                    }
                    onPasswordChange(newPassword)
                    if (confirmPassword.isNotEmpty()) {
                        confirmPasswordError = if (newPassword != confirmPassword) 
                            "Passwords do not match" 
                        else 
                            null
                    }
                },
                label = "Password",
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState: FocusState ->
                        if (focusState.isFocused) {
                            hasPasswordFocused = true
                            passwordComplexity = passwordRequirements.map { (requirement, validator) ->
                                requirement to validator(password)
                            }
                        }
                    },
                isError = hasPasswordFocused && (password.isBlank() || passwordComplexity.any { !it.second }),
                errorMessage = if (hasPasswordFocused && password.isBlank()) "Password is required" else null,
                enabled = !isLoading
            )

            if (hasPasswordFocused && passwordComplexity.isNotEmpty() && !passwordComplexity.all { it.second }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    passwordComplexity.forEach { (requirement, isMet) ->
                        Text(
                            text = requirement,
                            color = if (isMet) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = confirmPassword,
                onValueChange = { newValue -> 
                    confirmPassword = newValue
                    if (hasConfirmPasswordFocused) {
                        confirmPasswordError = when {
                            newValue.isBlank() -> "Confirm password is required"
                            newValue != password -> "Passwords do not match"
                            else -> null
                        }
                    }
                },
                label = "Confirm Password",
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState: FocusState ->
                        if (focusState.isFocused) {
                            hasConfirmPasswordFocused = true
                        } else if (hasConfirmPasswordFocused && confirmPassword.isBlank()) {
                            confirmPasswordError = "Confirm password is required"
                        }
                    },
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onRegisterClick(email, password, confirmPassword) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && 
                         emailError == null && 
                         passwordComplexity.all { it.second } && 
                         confirmPasswordError == null &&
                         email.isNotBlank() &&
                         password.isNotBlank() &&
                         confirmPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Register")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Text("Already have an account? Login")
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
