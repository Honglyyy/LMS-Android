package com.lms.lms.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.CategoryResponseDTO
import com.lms.lms.data.model.CourseResponseDTO
import com.lms.lms.data.model.EnrollmentResponseDTO
import com.lms.lms.data.model.LessonResponseDTO
import com.lms.lms.data.model.SectionResponseDTO
import kotlinx.coroutines.launch

class InstructorViewModel : ViewModel() {
    var categories by mutableStateOf<List<CategoryResponseDTO>>(emptyList())
        private set

    var courses by mutableStateOf<List<CourseResponseDTO>>(emptyList())
        private set

    var enrollments by mutableStateOf<List<EnrollmentResponseDTO>>(emptyList())
        private set

    var sections by mutableStateOf<List<SectionResponseDTO>>(emptyList())
        private set

    var lessons by mutableStateOf<List<LessonResponseDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadDashboardData()
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.getAllCategories()
                if (res.isSuccessful) categories = res.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val cr = NetworkClient.apiService.getMyCourses()
                val sc = NetworkClient.apiService.getMySections()
                val ls = NetworkClient.apiService.getMyLessons()
                
                if (cr.isSuccessful) courses = cr.body() ?: emptyList()
                if (sc.isSuccessful) sections = sc.body() ?: emptyList()
                if (ls.isSuccessful) lessons = ls.body() ?: emptyList()

                val allEnrollments = mutableListOf<EnrollmentResponseDTO>()
                courses.forEach { course ->
                    try {
                        val er = NetworkClient.apiService.getCourseEnrollments(course.courseId)
                        if (er.isSuccessful) {
                            er.body()?.let { allEnrollments.addAll(it) }
                        }
                    } catch (_: Exception) {}
                }
                enrollments = allEnrollments
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}
