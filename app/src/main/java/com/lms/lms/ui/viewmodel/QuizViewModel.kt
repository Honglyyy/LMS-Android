package com.lms.lms.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {
    var lessonDetail by mutableStateOf<LessonQuizDTO?>(null)
        private set
    
    var questions by mutableStateOf<List<QuestionDetailDTO>>(emptyList())
        private set

    var answers by mutableStateOf<List<AnswerDetailDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadQuizzes(lessonId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val res = NetworkClient.apiService.getLesson(lessonId)
                if (res.isSuccessful) lessonDetail = res.body()
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun loadQuestions(quizId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val qRes = NetworkClient.apiService.getQuestions()
                val aRes = NetworkClient.apiService.getAnswers()
                if (qRes.isSuccessful && aRes.isSuccessful) {
                    val allAnswers = aRes.body() ?: emptyList()
                    questions = qRes.body()?.filter { it.quizId == quizId }?.map { q ->
                        QuestionDetailDTO(
                            questionId = q.questionId,
                            questionText = q.questionText,
                            point = 0,
                            answers = allAnswers.filter { it.questionId == q.questionId }.map {
                                AnswerDetailDTO(it.answerId, it.answerText, it.isCorrect)
                            }
                        )
                    } ?: emptyList()
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun loadAnswers(questionId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                val res = NetworkClient.apiService.getAnswers()
                if (res.isSuccessful) {
                    answers = res.body()?.filter { it.questionId == questionId }?.map {
                        AnswerDetailDTO(it.answerId, it.answerText, it.isCorrect)
                    } ?: emptyList()
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    // --- Quiz CRUD ---
    fun createQuiz(dto: QuizCreateDTO, lessonId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.createQuiz(dto).isSuccessful) loadQuizzes(lessonId)
            } catch (_: Exception) {}
        }
    }
    fun updateQuiz(quizId: Long, dto: QuizCreateDTO, lessonId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.updateQuiz(quizId, dto).isSuccessful) loadQuizzes(lessonId)
            } catch (_: Exception) {}
        }
    }
    fun deleteQuiz(quizId: Long, lessonId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.deleteQuiz(quizId).isSuccessful) loadQuizzes(lessonId)
            } catch (_: Exception) {}
        }
    }

    // --- Question CRUD ---
    fun createQuestion(dto: QuestionCreateDTO, quizId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.createQuestion(dto).isSuccessful) loadQuestions(quizId)
            } catch (_: Exception) {}
        }
    }
    fun updateQuestion(questionId: Long, dto: QuestionCreateDTO, quizId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.updateQuestion(questionId, dto).isSuccessful) loadQuestions(quizId)
            } catch (_: Exception) {}
        }
    }
    fun deleteQuestion(questionId: Long, quizId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.deleteQuestion(questionId).isSuccessful) loadQuestions(quizId)
            } catch (_: Exception) {}
        }
    }

    // --- Answer CRUD ---
    fun createAnswer(dto: AnswerCreateDTO, questionId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.createAnswer(dto).isSuccessful) loadAnswers(questionId)
            } catch (_: Exception) {}
        }
    }
    fun updateAnswer(answerId: Long, dto: AnswerCreateDTO, questionId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.updateAnswer(answerId, dto).isSuccessful) loadAnswers(questionId)
            } catch (_: Exception) {}
        }
    }
    fun deleteAnswer(answerId: Long, questionId: Long) {
        viewModelScope.launch {
            try {
                if (NetworkClient.apiService.deleteAnswer(answerId).isSuccessful) loadAnswers(questionId)
            } catch (_: Exception) {}
        }
    }
}
