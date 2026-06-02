package com.lms.lms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.ui.auth.*
import com.lms.lms.ui.instructor.InstructorApp
import com.lms.lms.ui.shared.LmsTheme
import com.lms.lms.ui.student.StudentApp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lms.lms.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkClient.init(applicationContext)
        setContent {
            LmsTheme {
                LmsNavHost()
            }
        }
    }
}

// ── Navigation states ─────────────────────────────────────────────────────────
sealed class Screen {
    object Login          : Screen()
    object Register       : Screen()
    data class OtpVerify(val email: String) : Screen()
    object ForgotPassword : Screen()
    object StudentHome    : Screen()
    object InstructorHome : Screen()
}

@Composable
fun LmsNavHost() {
    val authViewModel: AuthViewModel = viewModel()
    
    // Check if already logged in
    val initialScreen = remember {
        if (NetworkClient.isLoggedIn()) {
            when (NetworkClient.getRole()?.uppercase()) {
                "INSTRUCTOR", "ADMIN" -> Screen.InstructorHome
                else -> Screen.StudentHome
            }
        } else Screen.Login
    }

    var current by remember { mutableStateOf<Screen>(initialScreen) }

    when (val screen = current) {
        is Screen.Login -> LoginScreen(
            onLoginSuccess = { role ->
                current = when (role.uppercase()) {
                    "INSTRUCTOR", "ADMIN" -> Screen.InstructorHome
                    else -> Screen.StudentHome
                }
            },
            onNavigateToRegister     = { current = Screen.Register },
            onNavigateToForgotPassword = { current = Screen.ForgotPassword },
            viewModel = authViewModel
        )

        is Screen.Register -> RegisterScreen(
            onRegisterSuccess = { email -> current = Screen.OtpVerify(email) },
            onNavigateToLogin = { current = Screen.Login },
            viewModel = authViewModel
        )

        is Screen.OtpVerify -> OtpVerificationScreen(
            email      = screen.email,
            onVerified = { current = Screen.Login },
            onBack     = { current = Screen.Login },
            viewModel = authViewModel
        )

        is Screen.ForgotPassword -> ForgotPasswordScreen(
            onBack = { current = Screen.Login },
            viewModel = authViewModel
        )

        is Screen.StudentHome -> StudentApp(
            onLogout = { 
                NetworkClient.clearSession()
                current = Screen.Login 
            }
        )

        is Screen.InstructorHome -> InstructorApp(
            onLogout = { 
                NetworkClient.clearSession()
                current = Screen.Login 
            }
        )
    }
}
