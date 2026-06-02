package com.lms.lms.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.CourseCreateDTO
import com.lms.lms.data.model.CourseDetailDTO
import com.lms.lms.data.model.CourseResponseDTO
import com.lms.lms.data.model.LessonCreateDTO
import com.lms.lms.data.model.SectionCreateDTO
import kotlinx.coroutines.launch

class CourseManagementViewModel : ViewModel() {
    var courses by mutableStateOf<List<CourseResponseDTO>>(emptyList())
        private set

    var courseDetail by mutableStateOf<CourseDetailDTO?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadMyCourses() {
        viewModelScope.launch {
            isLoading = true
            try {
                val res = NetworkClient.apiService.getMyCourses()
                if (res.isSuccessful) courses = res.body() ?: emptyList()
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun loadCourseDetail(courseId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val res = NetworkClient.apiService.getCourseDetail(courseId)
                if (res.isSuccessful) courseDetail = res.body()
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun createCourse(dto: CourseCreateDTO, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.createMyCourse(dto)
                if (res.isSuccessful) {
                    loadMyCourses()
                    onSuccess()
                } else onError("Failed to create course")
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    fun updateCourse(courseId: Long, dto: CourseCreateDTO, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.updateMyCourse(courseId, dto)
                if (res.isSuccessful) {
                    loadMyCourses()
                    onSuccess()
                } else onError("Failed to update course")
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    fun deleteCourse(courseId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.deleteMyCourse(courseId)
                if (res.isSuccessful) {
                    loadMyCourses()
                    onSuccess()
                } else onError("Failed to delete course")
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    // --- Section & Lesson management ---
    
    fun createSection(dto: SectionCreateDTO, courseId: Long) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.createMySection(dto)
                if (res.isSuccessful) loadCourseDetail(courseId)
            } catch (_: Exception) {}
        }
    }

    fun updateSection(sectionId: Long, dto: SectionCreateDTO, courseId: Long) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.updateMySection(sectionId, dto)
                if (res.isSuccessful) loadCourseDetail(courseId)
            } catch (_: Exception) {}
        }
    }

    fun deleteSection(sectionId: Long, courseId: Long) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.deleteMySection(sectionId)
                if (res.isSuccessful) loadCourseDetail(courseId)
            } catch (_: Exception) {}
        }
    }

    fun createLesson(dto: LessonCreateDTO, courseId: Long) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.createMyLesson(dto)
                if (res.isSuccessful) loadCourseDetail(courseId)
            } catch (_: Exception) {}
        }
    }

    fun updateLesson(lessonId: Long, dto: LessonCreateDTO, courseId: Long) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.updateMyLesson(lessonId, dto)
                if (res.isSuccessful) loadCourseDetail(courseId)
            } catch (_: Exception) {}
        }
    }

    fun deleteLesson(lessonId: Long, courseId: Long) {
        viewModelScope.launch {
            try {
                val res = NetworkClient.apiService.deleteMyLesson(lessonId)
                if (res.isSuccessful) loadCourseDetail(courseId)
            } catch (_: Exception) {}
        }
    }
}
