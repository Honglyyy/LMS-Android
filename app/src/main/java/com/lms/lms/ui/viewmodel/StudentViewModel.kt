package com.lms.lms.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {
    // Home/Browse/Learning states
    var courses by mutableStateOf<List<CourseResponseDTO>>(emptyList())
        private set
    var categories by mutableStateOf<List<CategoryResponseDTO>>(emptyList())
        private set
    var enrollments by mutableStateOf<List<EnrollmentResponseDTO>>(emptyList())
        private set
    var payments by mutableStateOf<List<PaymentResponseDTO>>(emptyList())
        private set

    var isLoadingHome by mutableStateOf(false)
        private set
    var isLoadingLearning by mutableStateOf(false)
        private set
    var isLoadingProfile by mutableStateOf(false)
        private set

    // Course Detail states
    var courseDetail by mutableStateOf<CourseDetailDTO?>(null)
    var isEnrolling by mutableStateOf(false)
    var isLoadingDetail by mutableStateOf(false)

    // Category Detail states
    var categoryDetail by mutableStateOf<CategoryDetailDTO?>(null)
    var isLoadingCategory by mutableStateOf(false)

    fun loadHomeData() {
        viewModelScope.launch {
            isLoadingHome = true
            try {
                val cr = NetworkClient.apiService.getAllCourses()
                val ca = NetworkClient.apiService.getAllCategories()
                if (cr.isSuccessful) courses = cr.body() ?: emptyList()
                if (ca.isSuccessful) categories = ca.body() ?: emptyList()
            } catch (_: Exception) {}
            isLoadingHome = false
        }
    }

    fun loadLearningData() {
        viewModelScope.launch {
            isLoadingLearning = true
            try {
                val res = NetworkClient.apiService.getMyEnrollments()
                if (res.isSuccessful) enrollments = res.body() ?: emptyList()
            } catch (_: Exception) {}
            isLoadingLearning = false
        }
    }

    fun loadProfileData() {
        viewModelScope.launch {
            isLoadingProfile = true
            try {
                val res = NetworkClient.apiService.getMyPayments()
                if (res.isSuccessful) payments = res.body() ?: emptyList()
            } catch (_: Exception) {}
            isLoadingProfile = false
        }
    }

    fun loadCourseDetail(courseId: Long) {
        viewModelScope.launch {
            isLoadingDetail = true
            try {
                val cr = NetworkClient.apiService.getCourseDetail(courseId)
                if (cr.isSuccessful) courseDetail = cr.body()
                
                // Also refresh enrollments to check status
                val me = NetworkClient.apiService.getMyEnrollments()
                if (me.isSuccessful) enrollments = me.body() ?: emptyList()
            } catch (_: Exception) {}
            isLoadingDetail = false
        }
    }

    fun loadCategoryDetail(categoryId: Long) {
        viewModelScope.launch {
            isLoadingCategory = true
            try {
                val res = NetworkClient.apiService.getCategory(categoryId)
                if (res.isSuccessful) categoryDetail = res.body()
            } catch (_: Exception) {}
            isLoadingCategory = false
        }
    }

    fun enrollInCourse(courseId: Long, isPaid: Boolean, onResult: (String) -> Unit) {
        viewModelScope.launch {
            isEnrolling = true
            try {
                if (isPaid) {
                    val res = NetworkClient.apiService.createCheckout(
                        PaymentCheckoutRequestDTO(courseId = courseId, provider = "STRIPE")
                    )
                    if (res.isSuccessful) {
                        val payment = res.body()
                        if (payment != null) {
                            NetworkClient.apiService.confirmPayment(payment.paymentId)
                            onResult("Course purchased successfully!")
                            loadLearningData()
                        }
                    } else {
                        onResult("Purchase failed: ${res.code()}")
                    }
                } else {
                    val res = NetworkClient.apiService.enroll(EnrollmentCreateDTO(courseId))
                    if (res.isSuccessful) {
                        enrollments = enrollments + res.body()!!
                        onResult("Successfully enrolled!")
                    } else {
                        onResult("Enrollment failed: ${res.code()}")
                    }
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
            isEnrolling = false
        }
    }

    fun submitReview(courseId: Long, rating: Int, reviewText: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                NetworkClient.apiService.addReview(
                    courseId,
                    CourseReviewCreateDTO(reviewText = reviewText, rating = rating)
                )
                onResult("Review submitted!")
                loadCourseDetail(courseId)
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }

    suspend fun loadLessonQuizzes(lessonId: Long): List<QuizDetailDTO> {
        return try {
            val res = NetworkClient.apiService.getLesson(lessonId)
            if (res.isSuccessful) res.body()?.quizz ?: emptyList()
            else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
