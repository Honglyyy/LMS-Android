package com.lms.lms.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import com.lms.lms.ui.shared.*
import kotlinx.coroutines.launch

// ── Login Screen ──────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LmsColors.Indigo900, LmsColors.Indigo800, LmsColors.Indigo600)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .alpha(0.08f)
                .background(Color.White, RoundedCornerShape(150.dp))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .alpha(0.06f)
                .background(LmsColors.Amber500, RoundedCornerShape(100.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "LearnHub",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Your gateway to knowledge",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(40.dp))

            // Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Welcome back",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LmsColors.OnSurface
                    )
                    Text(
                        "Sign in to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LmsColors.Subtitle
                    )
                    Spacer(Modifier.height(24.dp))

                    LmsTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = "Email",
                        leadingIcon = Icons.Outlined.Email
                    )
                    Spacer(Modifier.height(14.dp))
                    LmsTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true
                    )

                    AnimatedVisibility(visible = error != null) {
                        Text(
                            text = error ?: "",
                            color = LmsColors.Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Forgot password?", color = LmsColors.Indigo600)
                    }

                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(
                        text = "Sign In",
                        onClick = {
                            scope.launch {
                                loading = true
                                error = null
                                try {
                                    val res = NetworkClient.apiService.login(
                                        AuthRequest(email.trim(), password)
                                    )
                                    if (res.isSuccessful && res.body() != null) {
                                        val token = res.body()!!.string().trim()
                                        NetworkClient.saveToken(token)
                                        NetworkClient.saveEmail(email.trim())
                                        // Decode role from JWT or fetch user info
                                        val role = decodeRoleFromToken(token)
                                        NetworkClient.saveRole(role)
                                        onLoginSuccess(role)
                                    } else {
                                        error = "Invalid credentials or unverified account"
                                    }
                                } catch (e: Exception) {
                                    error = "Network error: ${e.message}"
                                }
                                loading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        loading = loading,
                        enabled = email.isNotBlank() && password.isNotBlank()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = Color.White.copy(alpha = 0.75f))
                TextButton(onClick = onNavigateToRegister) {
                    Text("Sign Up", color = LmsColors.Amber500, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Register Screen ───────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    onRegisterSuccess: (email: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LmsColors.Indigo900, LmsColors.Indigo600)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Text(
                "LearnHub",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Create your account",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Join as",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LmsColors.OnSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("USER" to "Student", "INSTRUCTOR" to "Instructor").forEach { (role, label) ->
                            val selected = selectedRole == role
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = role },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) LmsColors.Indigo600 else LmsColors.Indigo50,
                                border = if (selected) null else BorderStroke(1.dp, LmsColors.Indigo200)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        if (role == "USER") Icons.Filled.Person else Icons.Filled.School,
                                        null,
                                        tint = if (selected) Color.White else LmsColors.Indigo600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        label,
                                        color = if (selected) Color.White else LmsColors.Indigo600,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    LmsTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        leadingIcon = Icons.Outlined.Person
                    )
                    Spacer(Modifier.height(12.dp))
                    LmsTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = Icons.Outlined.Email
                    )
                    Spacer(Modifier.height(12.dp))
                    LmsTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true
                    )

                    AnimatedVisibility(visible = error != null) {
                        Text(
                            error ?: "",
                            color = LmsColors.Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = "Create Account",
                        onClick = {
                            scope.launch {
                                loading = true
                                error = null
                                try {
                                    val res = NetworkClient.apiService.register(
                                        RegisterRequest(email.trim(), username.trim(), password, selectedRole)
                                    )
                                    if (res.isSuccessful) {
                                        onRegisterSuccess(email.trim())
                                    } else {
                                        error = "Registration failed. Try a different email."
                                    }
                                } catch (e: Exception) {
                                    error = "Network error: ${e.message}"
                                }
                                loading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        loading = loading,
                        enabled = email.isNotBlank() && username.isNotBlank() && password.length >= 6
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = Color.White.copy(alpha = 0.75f))
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = LmsColors.Amber500, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── OTP Verification Screen ───────────────────────────────────────────────────
@Composable
fun OtpVerificationScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var otp by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(LmsColors.Indigo50, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MarkEmailRead, null, tint = LmsColors.Indigo600, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Check your email", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "We sent a verification code to\n$email",
                style = MaterialTheme.typography.bodyMedium,
                color = LmsColors.Subtitle,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            LmsTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = "OTP Code",
                leadingIcon = Icons.Outlined.Pin
            )
            AnimatedVisibility(visible = error != null) {
                Text(error ?: "", color = LmsColors.Error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (success) "Verified ✓" else "Verify Account",
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            val res = NetworkClient.apiService.verifyOtp(VerifyOtpRequest(email, otp))
                            if (res.isSuccessful) {
                                success = true
                                kotlinx.coroutines.delay(800)
                                onVerified()
                            } else {
                                error = "Invalid OTP. Please try again."
                            }
                        } catch (e: Exception) {
                            error = "Error: ${e.message}"
                        }
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                loading = loading,
                enabled = otp.length >= 4
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Login")
            }
        }
    }
}

// ── Forgot Password Screen ────────────────────────────────────────────────────
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) } // 0 = email, 1 = reset form
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(LmsColors.Amber500.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LockReset, null, tint = LmsColors.Amber500, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Reset Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                if (step == 0) "Enter your email to receive a reset code"
                else "Enter the OTP sent to your email",
                style = MaterialTheme.typography.bodyMedium,
                color = LmsColors.Subtitle,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            if (step == 0) {
                LmsTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = Icons.Outlined.Email
                )
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = "Send OTP",
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                NetworkClient.apiService.sendResetOtp(email.trim())
                                message = "OTP sent to $email"
                                step = 1
                            } catch (e: Exception) {
                                error = "Error: ${e.message}"
                            }
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = loading,
                    enabled = email.isNotBlank()
                )
            } else {
                LmsTextField(value = otp, onValueChange = { otp = it }, label = "OTP Code", leadingIcon = Icons.Outlined.Pin)
                Spacer(Modifier.height(12.dp))
                LmsTextField(value = newPassword, onValueChange = { newPassword = it }, label = "New Password", isPassword = true, leadingIcon = Icons.Outlined.Lock)
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = "Reset Password",
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                val res = NetworkClient.apiService.resetPassword(
                                    ResetPasswordRequest(email = email.trim(), password = newPassword, otp = otp)
                                )
                                if (res.isSuccessful) {
                                    message = "Password reset! Please login."
                                    kotlinx.coroutines.delay(1500)
                                    onBack()
                                } else {
                                    error = "Invalid OTP or expired."
                                }
                            } catch (e: Exception) {
                                error = "Error: ${e.message}"
                            }
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = loading,
                    enabled = otp.isNotBlank() && newPassword.length >= 6
                )
            }

            message?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = LmsColors.Success, style = MaterialTheme.typography.bodySmall)
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = LmsColors.Error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Back to Login")
            }
        }
    }
}

// ── JWT decode helper (base64 decode payload) ─────────────────────────────────
fun decodeRoleFromToken(token: String): String {
    return try {
        val payload = token.split(".")[1]
        val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
        val json = org.json.JSONObject(decoded)
        // Spring Security typically stores roles in "roles" array or "role" claim
        when {
            json.has("roles") -> {
                val roles = json.getJSONArray("roles")
                val roleStr = roles.getString(0)
                when {
                    roleStr.contains("INSTRUCTOR") -> "INSTRUCTOR"
                    roleStr.contains("ADMIN") -> "ADMIN"
                    else -> "USER"
                }
            }
            json.has("role") -> json.getString("role")
            else -> "USER"
        }
    } catch (_: Exception) { "USER" }
}