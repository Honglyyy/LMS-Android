package com.lms.lms.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.util.Base64

class AuthViewModel : ViewModel() {
    // Login state
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var isLoginLoading by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)

    // Register state
    var regEmail by mutableStateOf("")
    var regUsername by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regSelectedRole by mutableStateOf("USER")
    var isRegLoading by mutableStateOf(false)
    var regError by mutableStateOf<String?>(null)

    // OTP state
    var otpCode by mutableStateOf("")
    var isOtpLoading by mutableStateOf(false)
    var otpError by mutableStateOf<String?>(null)
    var isOtpSuccess by mutableStateOf(false)

    // Forgot Password state
    var forgotStep by mutableStateOf(0) // 0 = email, 1 = reset form
    var forgotEmail by mutableStateOf("")
    var forgotOtp by mutableStateOf("")
    var forgotNewPassword by mutableStateOf("")
    var isForgotLoading by mutableStateOf(false)
    var forgotMessage by mutableStateOf<String?>(null)
    var forgotError by mutableStateOf<String?>(null)

    fun login(onSuccess: (role: String) -> Unit) {
        viewModelScope.launch {
            isLoginLoading = true
            loginError = null
            try {
                val res = NetworkClient.apiService.login(
                    AuthRequest(loginEmail.trim(), loginPassword)
                )
                if (res.isSuccessful && res.body() != null) {
                    val token = res.body()!!.string().trim()
                    NetworkClient.saveToken(token)
                    NetworkClient.saveEmail(loginEmail.trim())
                    val role = decodeRoleFromToken(token)
                    NetworkClient.saveRole(role)
                    onSuccess(role)
                } else {
                    loginError = "Invalid credentials or unverified account"
                }
            } catch (e: Exception) {
                loginError = "Network error: ${e.message}"
            }
            isLoginLoading = false
        }
    }

    fun register(onSuccess: (email: String) -> Unit) {
        viewModelScope.launch {
            isRegLoading = true
            regError = null
            try {
                val res = NetworkClient.apiService.register(
                    RegisterRequest(regEmail.trim(), regUsername.trim(), regPassword, regSelectedRole)
                )
                if (res.isSuccessful) {
                    onSuccess(regEmail.trim())
                } else {
                    regError = "Registration failed. Try a different email."
                }
            } catch (e: Exception) {
                regError = "Network error: ${e.message}"
            }
            isRegLoading = false
        }
    }

    fun verifyOtp(email: String, onVerified: () -> Unit) {
        viewModelScope.launch {
            isOtpLoading = true
            otpError = null
            try {
                val res = NetworkClient.apiService.verifyOtp(VerifyOtpRequest(email, otpCode))
                if (res.isSuccessful) {
                    isOtpSuccess = true
                    delay(800)
                    onVerified()
                } else {
                    otpError = "Invalid OTP. Please try again."
                }
            } catch (e: Exception) {
                otpError = "Error: ${e.message}"
            }
            isOtpLoading = false
        }
    }

    fun sendResetOtp() {
        viewModelScope.launch {
            isForgotLoading = true
            forgotError = null
            try {
                NetworkClient.apiService.sendResetOtp(forgotEmail.trim())
                forgotMessage = "OTP sent to $forgotEmail"
                forgotStep = 1
            } catch (e: Exception) {
                forgotError = "Error: ${e.message}"
            }
            isForgotLoading = false
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isForgotLoading = true
            forgotError = null
            try {
                val res = NetworkClient.apiService.resetPassword(
                    ResetPasswordRequest(email = forgotEmail.trim(), password = forgotNewPassword, otp = forgotOtp)
                )
                if (res.isSuccessful) {
                    forgotMessage = "Password reset! Please login."
                    delay(1500)
                    onSuccess()
                } else {
                    forgotError = "Invalid OTP or expired."
                }
            } catch (e: Exception) {
                forgotError = "Error: ${e.message}"
            }
            isForgotLoading = false
        }
    }

    private fun decodeRoleFromToken(token: String): String {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE))
            val json = JSONObject(decoded)
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
}
