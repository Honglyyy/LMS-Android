package com.lms.lms.data.api

import com.lms.lms.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("authenticate")
    suspend fun login(@Body request: AuthRequest): Response<ResponseBody>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponseDTO>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<UserResponseDTO>

    @POST("send-reset-otp")
    suspend fun sendResetOtp(@Query("email") email: String): Response<Unit>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResponseBody>

    // ── Users ─────────────────────────────────────────────────────────────────
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserResponseDTO>>

    @PATCH("api/users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Long,
        @Body request: UpdateRoleRequest
    ): Response<UserResponseDTO>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>

    // ── Courses ───────────────────────────────────────────────────────────────
    @GET("api/courses")
    suspend fun getAllCourses(): Response<List<CourseResponseDTO>>

    @GET("api/courses/{id}")
    suspend fun getCourseDetail(@Path("id") id: Long): Response<CourseDetailDTO>

    @POST("api/courses")
    suspend fun createCourse(@Body dto: CourseCreateDTO): Response<CourseResponseDTO>

    @PUT("api/courses/{id}")
    suspend fun updateCourse(@Path("id") id: Long, @Body dto: CourseCreateDTO): Response<CourseResponseDTO>

    @DELETE("api/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: Long): Response<String>

    @GET("api/courses/instructor/me")
    suspend fun getMyCourses(): Response<List<CourseResponseDTO>>

    @POST("api/courses/instructor/me")
    suspend fun createMyCourse(@Body dto: CourseCreateDTO): Response<CourseResponseDTO>

    @PUT("api/courses/instructor/me/{id}")
    suspend fun updateMyCourse(@Path("id") id: Long, @Body dto: CourseCreateDTO): Response<CourseResponseDTO>

    @DELETE("api/courses/instructor/me/{id}")
    suspend fun deleteMyCourse(@Path("id") id: Long): Response<Unit>

    // ── Categories ────────────────────────────────────────────────────────────
    @GET("api/categories")
    suspend fun getAllCategories(): Response<List<CategoryResponseDTO>>

    @GET("api/categories/{id}")
    suspend fun getCategory(@Path("id") id: Long): Response<CategoryDetailDTO>

    @POST("api/categories")
    suspend fun createCategory(@Body category: CategoryRequest): Response<CategoryResponseDTO>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: CategoryRequest): Response<CategoryResponseDTO>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): Response<String>

    // ── Sections ──────────────────────────────────────────────────────────────
    @GET("api/sections")
    suspend fun getSections(): Response<List<SectionResponseDTO>>

    @POST("api/sections")
    suspend fun createSection(@Body dto: SectionCreateDTO): Response<SectionResponseDTO>

    @PUT("api/sections/{id}")
    suspend fun updateSection(@Path("id") id: Long, @Body dto: SectionCreateDTO): Response<SectionResponseDTO>

    @DELETE("api/sections/{id}")
    suspend fun deleteSection(@Path("id") id: Long): Response<String>

    @GET("api/sections/instructor/me")
    suspend fun getMySections(): Response<List<SectionResponseDTO>>

    @POST("api/sections/instructor/me")
    suspend fun createMySection(@Body dto: SectionCreateDTO): Response<SectionResponseDTO>

    @PUT("api/sections/instructor/me/{id}")
    suspend fun updateMySection(@Path("id") id: Long, @Body dto: SectionCreateDTO): Response<SectionResponseDTO>

    @DELETE("api/sections/instructor/me/{id}")
    suspend fun deleteMySection(@Path("id") id: Long): Response<Unit>

    // ── Lessons ───────────────────────────────────────────────────────────────
    @GET("api/lessons")
    suspend fun getLessons(): Response<List<LessonResponseDTO>>

    @GET("api/lessons/{id}")
    suspend fun getLesson(@Path("id") id: Long): Response<LessonQuizDTO>

    @POST("api/lessons")
    suspend fun createLesson(@Body dto: LessonCreateDTO): Response<LessonResponseDTO>

    @PUT("api/lessons/{id}")
    suspend fun updateLesson(@Path("id") id: Long, @Body dto: LessonCreateDTO): Response<LessonResponseDTO>

    @DELETE("api/lessons/{id}")
    suspend fun deleteLesson(@Path("id") id: Long): Response<String>

    @GET("api/lessons/instructor/me")
    suspend fun getMyLessons(): Response<List<LessonResponseDTO>>

    @POST("api/lessons/instructor/me")
    suspend fun createMyLesson(@Body dto: LessonCreateDTO): Response<LessonResponseDTO>

    @PUT("api/lessons/instructor/me/{id}")
    suspend fun updateMyLesson(@Path("id") id: Long, @Body dto: LessonCreateDTO): Response<LessonResponseDTO>

    @DELETE("api/lessons/instructor/me/{id}")
    suspend fun deleteMyLesson(@Path("id") id: Long): Response<Unit>

    // ── Quizzes ───────────────────────────────────────────────────────────────
    @GET("api/quizzes")
    suspend fun getQuizzes(): Response<List<QuizResponseDTO>>

    @POST("api/quizzes")
    suspend fun createQuiz(@Body dto: QuizCreateDTO): Response<QuizResponseDTO>

    @PUT("api/quizzes/{id}")
    suspend fun updateQuiz(@Path("id") id: Long, @Body dto: QuizCreateDTO): Response<QuizResponseDTO>

    @DELETE("api/quizzes/{id}")
    suspend fun deleteQuiz(@Path("id") id: Long): Response<String>

    // ── Questions ─────────────────────────────────────────────────────────────
    @GET("api/questions")
    suspend fun getQuestions(): Response<List<QuestionResponseDTO>>

    @POST("api/questions")
    suspend fun createQuestion(@Body dto: QuestionCreateDTO): Response<QuestionResponseDTO>

    @PUT("api/questions/{id}")
    suspend fun updateQuestion(@Path("id") id: Long, @Body dto: QuestionCreateDTO): Response<QuestionResponseDTO>

    @DELETE("api/questions/{id}")
    suspend fun deleteQuestion(@Path("id") id: Long): Response<String>

    // ── Answers ───────────────────────────────────────────────────────────────
    @GET("api/answers")
    suspend fun getAnswers(): Response<List<AnswerResponseDTO>>

    @POST("api/answers")
    suspend fun createAnswer(@Body dto: AnswerCreateDTO): Response<AnswerResponseDTO>

    @PUT("api/answers/{id}")
    suspend fun updateAnswer(@Path("id") id: Long, @Body dto: AnswerCreateDTO): Response<AnswerResponseDTO>

    @DELETE("api/answers/{id}")
    suspend fun deleteAnswer(@Path("id") id: Long): Response<String>

    // ── Enrollments ───────────────────────────────────────────────────────────
    @POST("api/enrollments")
    suspend fun enroll(@Body dto: EnrollmentCreateDTO): Response<EnrollmentResponseDTO>

    @GET("api/enrollments/me")
    suspend fun getMyEnrollments(): Response<List<EnrollmentResponseDTO>>

    @DELETE("api/enrollments/me/courses/{courseId}")
    suspend fun cancelEnrollment(@Path("courseId") courseId: Long): Response<Unit>

    @GET("api/enrollments")
    suspend fun getAllEnrollments(): Response<List<EnrollmentResponseDTO>>

    @GET("api/courses/{courseId}/enrollments")
    suspend fun getCourseEnrollments(@Path("courseId") courseId: Long): Response<List<EnrollmentResponseDTO>>

    @PATCH("api/enrollments/{id}/status")
    suspend fun updateEnrollmentStatus(
        @Path("id") id: Long,
        @Body dto: EnrollmentStatusUpdateDTO
    ): Response<EnrollmentResponseDTO>

    @DELETE("api/enrollments/{id}")
    suspend fun deleteEnrollment(@Path("id") id: Long): Response<Unit>

    // ── Reviews ───────────────────────────────────────────────────────────────
    @GET("api/reviews")
    suspend fun getAllReviews(): Response<List<CourseReviewResponseDTO>>

    @GET("api/courses/{courseId}/reviews")
    suspend fun getCourseReviews(@Path("courseId") courseId: Long): Response<List<CourseReviewResponseDTO>>

    @POST("api/courses/{courseId}/review")
    suspend fun addReview(
        @Path("courseId") courseId: Long,
        @Body dto: CourseReviewCreateDTO
    ): Response<CourseReviewResponseDTO>

    // ── Payments ──────────────────────────────────────────────────────────────
    @POST("api/payments/checkout")
    suspend fun createCheckout(@Body dto: PaymentCheckoutRequestDTO): Response<PaymentResponseDTO>

    @POST("api/payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: Long): Response<PaymentResponseDTO>

    @GET("api/payments/me")
    suspend fun getMyPayments(): Response<List<PaymentResponseDTO>>

    @GET("api/payments")
    suspend fun getAllPayments(): Response<List<PaymentResponseDTO>>

    // ── File Uploads ──────────────────────────────────────────────────────────
    @Multipart
    @POST("api/uploads/course-cover")
    suspend fun uploadCourseCover(
        @Part file: MultipartBody.Part
    ): Response<FileUploadResponseDTO>

    @Multipart
    @POST("api/uploads/lesson-video")
    suspend fun uploadLessonVideo(
        @Part file: MultipartBody.Part
    ): Response<FileUploadResponseDTO>
}