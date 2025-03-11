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

class RegistrationActivity : ComponentActivity(), AuthContract.RegistrationView {
    
    private lateinit var presenter: RegistrationPresenter
    private val snackbarHostState = SnackbarHostState()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        presenter = RegistrationPresenter(this)
        presenter.attachView(this)
        
        setContent {
            SecureMVPLabTheme {
                RegistrationScreen(
                    onRegisterClick = { username, password, confirmPassword ->
                        presenter.register(username, password, confirmPassword)
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
            snackbarHostState.showSnackbar(message)
        }
    }
    
    override fun showRegistrationSuccess() {
        lifecycleScope.launch {
            snackbarHostState.showSnackbar("Registration successful!")
            // Navigate back to login after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1500)
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
    snackbarHostState: SnackbarHostState
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordStrength by remember { mutableStateOf<Pair<Boolean, String>>(false to "") }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        onPasswordChange(it)
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                if (password.isNotEmpty()) {
                    Text(
                        text = passwordStrength.second,
                        color = if (passwordStrength.first) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        onRegisterClick(username, password, confirmPassword)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
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
} 