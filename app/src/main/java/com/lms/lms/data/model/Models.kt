package com.lms.lms.data.model

// ── Auth ──────────────────────────────────────────────────────────────────────
data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val role: String = "USER"          // sent as plain String; server maps to Roles enum
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

/**
 * Java: ResetPasswordRequest(String email, String password, String otp)
 * Field order in the record: email, password, otp  ← match exactly
 */
data class ResetPasswordRequest(
    val email: String,
    val password: String,
    val otp: String
)

data class UpdateRoleRequest(
    val role: String                   // e.g. "ADMIN", "USER", "INSTRUCTOR"
)

// ── Users ─────────────────────────────────────────────────────────────────────
/**
 * Java: UserResponseDTO(String userId, String username, String email,
 *                       String role, Boolean isVerified)
 */
data class UserResponseDTO(
    val userId: String,
    val username: String,
    val email: String,
    val role: String,
    val isVerified: Boolean
)

// ── Categories ────────────────────────────────────────────────────────────────
/**
 * Java: CategoryResponseDTO(Long categoryId, String category)
 */
data class CategoryResponseDTO(
    val categoryId: Long,
    val category: String               // ← NOT "name"
)

/**
 * Java: CategoryDetailDTO(Long categoryId, String category, List<CourseDTO> courses)
 */
data class CategoryDetailDTO(
    val categoryId: Long,
    val category: String,
    val courses: List<CourseDTO> = emptyList()
)

data class CategoryRequest(
    val category: String               // matches field name the server expects
)

// ── CourseDTO (used inside CategoryDetailDTO) ─────────────────────────────────
/**
 * Java: CourseDTO(Long courseId, String title, String description,
 *                 BigDecimal price, String overallDuration, String coverDir,
 *                 String instructor)
 */
data class CourseDTO(
    val courseId: Long,
    val title: String,
    val description: String?,
    val price: Double?,
    val overallDuration: String?,
    val coverDir: String?,             // ← NOT coverImageUrl
    val instructor: String?
)

// ── Courses ───────────────────────────────────────────────────────────────────
/**
 * Java: CourseResponseDTO(Long courseId, String title, String description,
 *                         BigDecimal price, String overallDuration, String coverDir,
 *                         Long instructorId, String instructor,
 *                         List<Long> categoryIds, List<String> categories,
 *                         Double rating)
 */
data class CourseResponseDTO(
    val courseId: Long,
    val title: String,
    val description: String?,
    val price: Double?,
    val overallDuration: String?,
    val coverDir: String?,             // ← NOT coverImageUrl
    val instructorId: Long?,
    val instructor: String?,           // ← NOT instructorName
    val categoryIds: List<Long> = emptyList(),
    val categories: List<String> = emptyList(),  // ← list of category names
    val rating: Double?
)

/**
 * Java: CourseDetailDTO(Long courseId, String title, String description,
 *                       BigDecimal price, String overallDuration, String coverDir,
 *                       String instructor, Long sectionCount, Double rating,
 *                       List<String> categories, List<SectionDetailDTO> sections,
 *                       List<CourseReviewResponseDTO> reviews)
 */
data class CourseDetailDTO(
    val courseId: Long,
    val title: String,
    val description: String?,
    val price: Double?,
    val overallDuration: String?,
    val coverDir: String?,
    val instructor: String?,
    val sectionCount: Long?,
    val rating: Double?,
    val categories: List<String> = emptyList(),
    val sections: List<SectionDetailDTO> = emptyList(),
    val reviews: List<CourseReviewResponseDTO> = emptyList()
)

/**
 * Java: CourseCreateDTO(String title, String description, BigDecimal price,
 *                       String overallDuration, String coverDir,
 *                       Long instructor, List<Long> categoryId)
 */
data class CourseCreateDTO(
    val title: String,
    val description: String?,
    val price: Double?,
    val overallDuration: String?,
    val coverDir: String?,             // ← NOT coverImageUrl
    val instructor: Long?,             // ← instructor ID, NOT instructorId
    val categoryId: List<Long>         // ← list, NOT single Long
)

// ── Sections ──────────────────────────────────────────────────────────────────
/**
 * Java: SectionResponseDTO(Long sectionId, String title, String duration,
 *                           Long courseId, String courseName)
 */
data class SectionResponseDTO(
    val sectionId: Long,
    val title: String,
    val duration: String?,
    val courseId: Long?,
    val courseName: String?            // ← NOT courseTitle
)

/**
 * Java: SectionDetailDTO(Long sectionId, String title, String duration,
 *                         Long lessonCount, List<LessonDetailDTO> lessons)
 */
data class SectionDetailDTO(
    val sectionId: Long,
    val title: String,
    val duration: String?,
    val lessonCount: Long?,
    val lessons: List<LessonDetailDTO> = emptyList()
)

/**
 * Java: SectionCreateDTO(String title, String duration, Long courseId)
 */
data class SectionCreateDTO(
    val title: String,
    val duration: String?,
    val courseId: Long
)

// ── Lessons ───────────────────────────────────────────────────────────────────
/**
 * Java: LessonResponseDTO(Long lessonId, String title, String videoDir,
 *                          Long sectionId, String sectionName)
 */
data class LessonResponseDTO(
    val lessonId: Long,
    val title: String,
    val videoDir: String?,             // ← NOT videoUrl
    val sectionId: Long?,
    val sectionName: String?           // ← NOT sectionTitle
)

/**
 * Java: LessonDetailDTO(Long lessonId, String title, String videoDir)
 */
data class LessonDetailDTO(
    val lessonId: Long,
    val title: String,
    val videoDir: String?
)

/**
 * Java: LessonQuizDTO(Long lessonId, String title, String videoDir,
 *                      List<QuizDetailDTO> quizz)   ← note: "quizz" plural
 */
data class LessonQuizDTO(
    val lessonId: Long,
    val title: String,
    val videoDir: String?,
    val quizz: List<QuizDetailDTO> = emptyList()   // ← "quizz" matches Java field
)

/**
 * Java: LessonCreateDTO(String title, String videoDir, Long sectionId)
 * Note: NO "content" field in the Java DTO!
 */
data class LessonCreateDTO(
    val title: String,
    val videoDir: String?,             // ← NOT videoUrl, NO content field
    val sectionId: Long
)

// ── Quizzes ───────────────────────────────────────────────────────────────────
/**
 * Java: QuizResponseDTO(Long quizId, String title, double totalPoints,
 *                        Long lessonId, String lessonName)
 */
data class QuizResponseDTO(
    val quizId: Long,
    val title: String,
    val totalPoints: Double,
    val lessonId: Long?,
    val lessonName: String?
)

/**
 * Java: QuizDetailDTO(Long quizId, String title, double totalPoints,
 *                      List<QuestionDetailDTO> question)   ← singular "question"
 */
data class QuizDetailDTO(
    val quizId: Long,
    val title: String,
    val totalPoints: Double,
    val question: List<QuestionDetailDTO> = emptyList()   // ← singular "question"
)

/**
 * Java: QuizCreateDTO(String title, double totalPoints, Long lessonId)
 */
data class QuizCreateDTO(
    val title: String,
    val totalPoints: Double,
    val lessonId: Long
)

// ── Questions ─────────────────────────────────────────────────────────────────
/**
 * Java: QuestionResponseDTO(Long questionId, String questionText, Long quizId)
 */
data class QuestionResponseDTO(
    val questionId: Long,
    val questionText: String,          // ← NOT "text"
    val quizId: Long?
)

/**
 * Java: QuestionDetailDTO(Long questionId, String questionText,
 *                          Long point, List<AnswerDetailDTO> answers)
 */
data class QuestionDetailDTO(
    val questionId: Long,
    val questionText: String,
    val point: Long?,
    val answers: List<AnswerDetailDTO> = emptyList()
)

/**
 * Java: QuestionCreateDTO(String questionText, Long quizId)
 */
data class QuestionCreateDTO(
    val questionText: String,          // ← NOT "text"
    val quizId: Long
)

// ── Answers ───────────────────────────────────────────────────────────────────
/**
 * Java: AnswerResponseDTO(Long answerId, String answerText,
 *                          Boolean isCorrect, Long questionId)
 */
data class AnswerResponseDTO(
    val answerId: Long,
    val answerText: String,            // ← NOT "text"
    val isCorrect: Boolean,
    val questionId: Long?
)

/**
 * Java: AnswerDetailDTO(Long answerId, String answerText, Boolean isCorrect)
 */
data class AnswerDetailDTO(
    val answerId: Long,
    val answerText: String,
    val isCorrect: Boolean
)

/**
 * Java: AnswerCreateDTO(String answerText, Boolean isCorrect, Long questionId)
 */
data class AnswerCreateDTO(
    val answerText: String,            // ← NOT "text"
    val isCorrect: Boolean,
    val questionId: Long
)

// ── Enrollments ───────────────────────────────────────────────────────────────
data class EnrollmentCreateDTO(val courseId: Long)

/**
 * Java: EnrollmentResponseDTO(Long enrollmentId, Long userId, String username,
 *                              String userEmail, Long courseId, String courseTitle,
 *                              String instructor, EnrollmentStatus status,
 *                              Timestamp enrolledAt, Timestamp updatedAt)
 */
data class EnrollmentResponseDTO(
    val enrollmentId: Long,
    val userId: Long?,
    val username: String?,
    val userEmail: String?,
    val courseId: Long?,
    val courseTitle: String?,
    val instructor: String?,
    val status: String?,               // deserialized from EnrollmentStatus enum → string
    val enrolledAt: String?,
    val updatedAt: String?
)

data class EnrollmentStatusUpdateDTO(
    val status: String                 // e.g. "ACTIVE", "COMPLETED", "CANCELLED"
)

// ── Reviews ───────────────────────────────────────────────────────────────────
/**
 * Java: CourseReviewCreateDTO(String reviewText, Integer rating)
 * Note: fields are reviewText + rating (NOT comment)
 */
data class CourseReviewCreateDTO(
    val reviewText: String,            // ← NOT "comment"
    val rating: Int
)

/**
 * Java: CourseReviewResponseDTO(Long reviewId, String reviewText,
 *                                Integer rating, String username, String courseTitle)
 */
data class CourseReviewResponseDTO(
    val reviewId: Long,
    val reviewText: String?,           // ← NOT "comment"
    val rating: Int,
    val username: String?,             // ← NOT "userEmail"
    val courseTitle: String?
)

// ── Payments ──────────────────────────────────────────────────────────────────
/**
 * Java: PaymentCheckoutRequestDTO(@NotNull Long courseId, String provider)
 */
data class PaymentCheckoutRequestDTO(
    val courseId: Long,
    val provider: String? = null
)

/**
 * Java: PaymentResponseDTO(Long paymentId, Long userId, String username,
 *                           String userEmail, Long courseId, String courseTitle,
 *                           BigDecimal amount, String provider,
 *                           String providerReference, PaymentStatus status,
 *                           Timestamp createdAt, Timestamp updatedAt)
 */
data class PaymentResponseDTO(
    val paymentId: Long,
    val userId: Long?,
    val username: String?,
    val userEmail: String?,
    val courseId: Long?,
    val courseTitle: String?,
    val amount: Double?,
    val provider: String?,
    val providerReference: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class PaymentStatusUpdateDTO(
    val status: String,
    val providerReference: String?
)
data class FileUploadResponseDTO(
    val originalFileName: String,
    val fileName: String,
    val contentType: String,
    val size: Long,
    val url: String
)