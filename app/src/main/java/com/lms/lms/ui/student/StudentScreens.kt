package com.lms.lms.ui.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import com.lms.lms.ui.shared.*
import kotlinx.coroutines.launch

// ── Student Bottom Nav ─────────────────────────────────────────────────────────
enum class StudentTab { HOME, BROWSE, MY_LEARNING, PROFILE }

@Composable
fun StudentApp(onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(StudentTab.HOME) }
    var selectedCourseId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    // Navigation Stack Logic
    // If selectedCourseId is set, show Detail Screen
    // If selectedCategoryId is set, show Category Screen (only if in Home tab)
    // Otherwise show the tab content

    BackHandler(enabled = selectedCourseId != null || selectedCategoryId != null) {
        if (selectedCourseId != null) {
            selectedCourseId = null
        } else if (selectedCategoryId != null) {
            selectedCategoryId = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf(
                        StudentTab.HOME        to (Icons.Filled.Home       to "Home"),
                        StudentTab.BROWSE      to (Icons.Filled.Explore    to "Browse"),
                        StudentTab.MY_LEARNING to (Icons.Filled.PlayCircle to "My Learning"),
                        StudentTab.PROFILE     to (Icons.Filled.Person     to "Profile")
                    ).forEach { (tab, iconLabel) ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick  = {
                                currentTab = tab
                                // Reset sub-navigation when switching tabs
                                selectedCourseId = null
                                selectedCategoryId = null
                            },
                            icon     = { Icon(iconLabel.first, null) },
                            label    = { Text(iconLabel.second) },
                            colors   = NavigationBarItemDefaults.colors(
                                selectedIconColor = LmsColors.Indigo600,
                                selectedTextColor = LmsColors.Indigo600,
                                indicatorColor    = LmsColors.Indigo50
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentTab) {
                    StudentTab.HOME -> {
                        StudentHomeScreen(
                            onCourseClick = { selectedCourseId = it },
                            onCategoryClick = { selectedCategoryId = it }
                        )
                    }
                    StudentTab.BROWSE -> BrowseCoursesScreen(onCourseClick = { selectedCourseId = it })
                    StudentTab.MY_LEARNING -> MyLearningScreen(onCourseClick = { selectedCourseId = it })
                    StudentTab.PROFILE -> StudentProfileScreen(onLogout = onLogout)
                }
            }
        }

        // Overlay screens for hierarchical navigation
        AnimatedVisibility(
            visible = currentTab == StudentTab.HOME && selectedCategoryId != null && selectedCourseId == null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            selectedCategoryId?.let { catId ->
                CategoryCoursesScreen(
                    categoryId = catId,
                    onBack = { selectedCategoryId = null },
                    onCourseClick = { selectedCourseId = it }
                )
            }
        }

        AnimatedVisibility(
            visible = selectedCourseId != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            selectedCourseId?.let { courseId ->
                CourseDetailScreen(
                    courseId = courseId,
                    onBack = { selectedCourseId = null }
                )
            }
        }
    }
}

// ── Quiz Taking Interface ───────────────────────────────────────────────────
@Composable
fun QuizTakingScreen(quiz: QuizDetailDTO, onDismiss: () -> Unit) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) } // QuestionId -> AnswerId
    var isFinished by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0.0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            if (!isFinished) {
                val question = quiz.question.getOrNull(currentQuestionIndex)
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(quiz.title, fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                            Text("Question ${currentQuestionIndex + 1} of ${quiz.question.size}", 
                                style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / quiz.question.size },
                        modifier = Modifier.fillMaxWidth().clip(CircleShape),
                        color = LmsColors.Indigo600,
                        trackColor = LmsColors.Indigo50
                    )
                    
                    if (question != null) {
                        Spacer(Modifier.height(32.dp))
                        Text(question.questionText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                        Spacer(Modifier.height(24.dp))
                        
                        question.answers.forEach { answer ->
                            val isSelected = selectedAnswers[question.questionId] == answer.answerId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedAnswers = selectedAnswers + (question.questionId to answer.answerId) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) LmsColors.Indigo50 else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) LmsColors.Indigo600 else LmsColors.Indigo900.copy(0.1f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = { selectedAnswers = selectedAnswers + (question.questionId to answer.answerId) })
                                    Spacer(Modifier.width(12.dp))
                                    Text(answer.answerText, color = LmsColors.Indigo900)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.weight(1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = { currentQuestionIndex-- },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Previous") }
                        }
                        
                        PrimaryButton(
                            text = if (currentQuestionIndex == quiz.question.size - 1) "Finish" else "Next",
                            onClick = {
                                if (currentQuestionIndex < quiz.question.size - 1) {
                                    currentQuestionIndex++
                                } else {
                                    // Calculate Score
                                    var correctCount = 0
                                    quiz.question.forEach { q ->
                                        val selectedAnswerId = selectedAnswers[q.questionId]
                                        val correctAnswer = q.answers.find { it.isCorrect }
                                        if (selectedAnswerId == correctAnswer?.answerId) {
                                            correctCount++
                                        }
                                    }
                                    val totalQuestions = quiz.question.size
                                    score = if (totalQuestions > 0) {
                                        (correctCount.toDouble() / totalQuestions) * quiz.totalPoints
                                    } else 0.0
                                    isFinished = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = question?.let { selectedAnswers.containsKey(it.questionId) } ?: false
                        )
                    }
                }
            } else {
                // Result Screen
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.EmojiEvents, null, tint = LmsColors.Amber500, modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Quiz Completed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("You scored", color = LmsColors.Subtitle)
                    Text("${score.toInt()} / ${quiz.totalPoints.toInt()}", 
                        style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = LmsColors.Indigo600)
                    Spacer(Modifier.height(48.dp))
                    PrimaryButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}


// ── Home Screen ───────────────────────────────────────────────────────────────
@Composable
fun StudentHomeScreen(
    onCourseClick: (Long) -> Unit,
    onCategoryClick: (Long) -> Unit
) {
    var courses       by remember { mutableStateOf<List<CourseResponseDTO>>(emptyList()) }
    var categories    by remember { mutableStateOf<List<CategoryResponseDTO>>(emptyList()) }
    var loading       by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val cr = NetworkClient.apiService.getAllCourses()
            val ca = NetworkClient.apiService.getAllCategories()
            if (cr.isSuccessful) courses = cr.body() ?: emptyList()
            if (ca.isSuccessful) categories = ca.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) { LoadingIndicator(); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(LmsColors.Indigo600, Color.White)
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Surface(
                        color = LmsColors.Amber500,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "NEW",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                             color = LmsColors.Indigo900
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Accelerate your\ncareer with LMS",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = LmsColors.Indigo900,
                        lineHeight = 32.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Learn from industry experts and get certified.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LmsColors.Indigo900.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Categories Row
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    "Top Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LmsColors.Indigo900,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { cat ->
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onCategoryClick(cat.categoryId) },
                            shape = RoundedCornerShape(16.dp),
                            color = LmsColors.Indigo50,
                            border = BorderStroke(1.dp, LmsColors.Indigo200.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = LmsColors.Indigo600,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    cat.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LmsColors.Indigo900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popular Courses
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Popular Courses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LmsColors.Indigo900,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = LmsColors.Indigo600
                )
            }
        }

        items(courses.take(5)) { course ->
            StudentCourseCard(
                course = course,
                onClick = { onCourseClick(course.courseId) }
            )
        }
    }
}

@Composable
fun StudentCourseCard(course: CourseResponseDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = LmsColors.Indigo50
            ) {
                AsyncImage(
                    model = buildFullUrl(course.coverDir ?: ""),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    course.categories.firstOrNull()?.uppercase() ?: "GENERAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = LmsColors.Indigo600,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LmsColors.Indigo900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = LmsColors.Amber500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${course.rating ?: 0.0} • ${course.instructor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LmsColors.Subtitle
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCoursesScreen(
    categoryId: Long,
    onBack: () -> Unit,
    onCourseClick: (Long) -> Unit
) {
    var categoryDetail by remember { mutableStateOf<CategoryDetailDTO?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(categoryId) {
        try {
            val res = NetworkClient.apiService.getCategory(categoryId)
            if (res.isSuccessful) categoryDetail = res.body()
        } catch (_: Exception) {}
        loading = false
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text(categoryDetail?.category ?: "Category", color = LmsColors.Indigo900) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = LmsColors.Indigo900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (loading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                categoryDetail?.courses?.let { courses ->
                    items(courses) { course ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clickable { onCourseClick(course.courseId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                             Row(modifier = Modifier.padding(12.dp)) {
                                 Surface(modifier = Modifier.size(70.dp), shape = RoundedCornerShape(8.dp), color = LmsColors.Indigo50) {
                                     AsyncImage(
                                         model = buildFullUrl(course.coverDir ?: ""),
                                         contentDescription = null,
                                         contentScale = ContentScale.Crop
                                     )
                                 }
                                 Spacer(Modifier.width(12.dp))
                                 Column {
                                     Text(course.title, fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                                     Text(course.instructor ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                                     Spacer(Modifier.height(4.dp))
                                     Text("${"%.2f".format(course.price ?: 0.0)}", fontWeight = FontWeight.ExtraBold, color = LmsColors.Indigo600)
                                 }
                             }
                        }
                    }
                }
            }
        }
    }
}



// ── Browse Screen ─────────────────────────────────────────────────────────────
@Composable
fun BrowseCoursesScreen(onCourseClick: (Long) -> Unit) {
    var courses by remember { mutableStateOf<List<CourseResponseDTO>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var search  by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val res = NetworkClient.apiService.getAllCourses()
            if (res.isSuccessful) courses = res.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    val filtered = if (search.isBlank()) courses
    else courses.filter {
        it.title.contains(search, ignoreCase = true) ||
        it.instructor?.contains(search, ignoreCase = true) == true ||
        it.categories.any { cat -> cat.contains(search, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(LmsColors.Indigo600, Color.White)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "Browse Courses",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LmsColors.Indigo900
                )
                Text(
                    text = "${courses.size} courses available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LmsColors.Indigo900.copy(alpha = 0.7f)
                )
            }
        }

        OutlinedTextField(
            value = search, onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Search courses, instructors...", color = LmsColors.Subtitle.copy(0.4f)) },
            leadingIcon  = { Icon(Icons.Outlined.Search, null, tint = LmsColors.Indigo900.copy(0.6f)) },
            trailingIcon = if (search.isNotBlank()) {
                { IconButton(onClick = { search = "" }) { Icon(Icons.Filled.Clear, null, tint = LmsColors.Indigo900) } }
            } else null,
            shape = RoundedCornerShape(16.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = LmsColors.Indigo400,
                unfocusedBorderColor = LmsColors.Indigo900.copy(0.1f),
                focusedContainerColor = LmsColors.Indigo50,
                unfocusedContainerColor = LmsColors.Indigo50,
                cursorColor = LmsColors.Indigo400,
                focusedTextColor = LmsColors.Indigo900,
                unfocusedTextColor = LmsColors.Indigo900
            )
        )
        Spacer(Modifier.height(16.dp))
        if (loading) { LoadingIndicator() } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filtered) { course ->
                    StudentCourseCard(
                        course = course,
                        onClick = { onCourseClick(course.courseId) }
                    )
                }
                if (filtered.isEmpty()) {
                    item { 
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.SearchOff, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No results for \"$search\"", color = Color.White.copy(0.5f))
                        }
                    }
                }
            }
        }
    }
}



// ── Course Detail Screen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(courseId: Long, onBack: () -> Unit) {
    val scope   = rememberCoroutineScope()
    var course        by remember { mutableStateOf<CourseDetailDTO?>(null) }
    var myEnrollments by remember { mutableStateOf<List<EnrollmentResponseDTO>>(emptyList()) }
    var loading       by remember { mutableStateOf(true) }
    var enrolling     by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var rating        by remember { mutableStateOf(5) }
    var reviewText    by remember { mutableStateOf("") }
    var activeLesson      by remember { mutableStateOf<LessonDetailDTO?>(null) }
    var isViewingVideo    by remember { mutableStateOf(false) }
    val snackbar      = remember { SnackbarHostState() }

    LaunchedEffect(courseId) {
        try {
            val cr = NetworkClient.apiService.getCourseDetail(courseId)
            val me = NetworkClient.apiService.getMyEnrollments()
            if (cr.isSuccessful) course        = cr.body()
            if (me.isSuccessful) myEnrollments = me.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    val isEnrolled = myEnrollments.any { it.courseId == courseId }

    if (isViewingVideo && activeLesson != null && !activeLesson?.videoDir.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { isViewingVideo = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                VideoPlayer(
                    videoUrl = buildFullUrl(activeLesson!!.videoDir!!),
                    modifier = Modifier.fillMaxSize(),
                    autoPlay = true,
                    onCloseClick = { isViewingVideo = false }
                )
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (!isViewingVideo) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.2f), CircleShape)) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }, modifier = Modifier.background(Color.Black.copy(0.2f), CircleShape)) {
                            Icon(Icons.Default.Share, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = {
            if (!isEnrolled && !isViewingVideo) {
                course?.let { c ->
                    Surface(color = Color.White, border = BorderStroke(1.dp, LmsColors.Indigo900.copy(0.1f))) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Price", style = MaterialTheme.typography.labelSmall, color = LmsColors.Subtitle)
                                Text(if ((c.price ?: 0.0) > 0) "$%.2f".format(c.price) else "FREE",
                                    fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = LmsColors.Indigo900)
                            }
                            PrimaryButton(
                                text = if ((c.price ?: 0.0) > 0.0) "Buy Now" else "Enroll Now",
                                onClick = {
                                    scope.launch {
                                        enrolling = true
                                        try {
                                            if ((c.price ?: 0.0) > 0.0) {
                                                // 1. Create Checkout
                                                val res = NetworkClient.apiService.createCheckout(
                                                    PaymentCheckoutRequestDTO(courseId = courseId, provider = "STRIPE")
                                                )
                                                if (res.isSuccessful) {
                                                    val payment = res.body()
                                                    if (payment != null) {
                                                        // 2. Immediately Confirm Payment (Set to PAID)
                                                        NetworkClient.apiService.confirmPayment(payment.paymentId)
                                                        snackbar.showSnackbar("Course purchased successfully!")
                                                        
                                                        // Refresh state
                                                        val me = NetworkClient.apiService.getMyEnrollments()
                                                        if (me.isSuccessful) myEnrollments = me.body() ?: emptyList()
                                                    }
                                                } else {
                                                    snackbar.showSnackbar("Purchase failed: ${res.code()}")
                                                }
                                            } else {
                                                // Free course - just enroll
                                                val res = NetworkClient.apiService.enroll(EnrollmentCreateDTO(courseId))
                                                if (res.isSuccessful) {
                                                    myEnrollments = myEnrollments + res.body()!!
                                                    snackbar.showSnackbar("Successfully enrolled!")
                                                } else {
                                                    snackbar.showSnackbar("Enrollment failed: ${res.code()}")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Error: ${e.message}")
                                        }
                                        enrolling = false
                                    }
                                },
                                modifier = Modifier.weight(1.5f),
                                loading = enrolling
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { LmsSnackbarHost(snackbar) }
    ) { padding ->
        if (loading) { LoadingIndicator(Modifier.padding(padding)); return@Scaffold }
        course?.let { c ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = if (isViewingVideo) 0.dp else padding.calculateBottomPadding())) {
                // Header Image or Video Preview
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        AsyncImage(
                            model = buildFullUrl(c.coverDir ?: ""),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))
                        ))
                        
                        // Play button overlay to indicate video can be watched
                        if (isEnrolled) {
                            IconButton(
                                onClick = { 
                                    if (activeLesson != null) isViewingVideo = true
                                    else {
                                        // Auto-select first lesson if none active
                                        val firstLesson = c.sections.firstOrNull()?.lessons?.firstOrNull()
                                        if (firstLesson != null) {
                                            activeLesson = firstLesson
                                            isViewingVideo = true
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.Center).size(64.dp)
                                    .background(Color.White.copy(0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }

                // Info Section
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Surface(color = LmsColors.Indigo600, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                c.categories.firstOrNull()?.uppercase() ?: "GENERAL",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(c.title, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = LmsColors.Indigo600, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(c.instructor ?: "", color = LmsColors.Subtitle, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.Star, null, tint = LmsColors.Amber500, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${c.rating ?: 0.0} (Reviews)", color = LmsColors.Subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Features
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text("This course includes:", fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                        Spacer(Modifier.height(12.dp))
                        val features = listOf(
                            Icons.Default.Timer to "${c.overallDuration ?: "Flexible"} duration",
                            Icons.Default.List to "${c.sectionCount ?: c.sections.size} sections",
                            Icons.Default.AllInclusive to "Full lifetime access",
                            Icons.Default.CardMembership to "Certificate of completion"
                        )
                        features.forEach { (icon, text) ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, null, tint = LmsColors.Indigo600, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(text, color = LmsColors.Subtitle, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = LmsColors.Indigo900.copy(0.05f))
                    }
                }

                // Description
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Description", fontWeight = FontWeight.Bold, color = LmsColors.Indigo900, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(c.description ?: "No description provided.",
                            color = LmsColors.Subtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Curriculum - ONLY IF ENROLLED
                if (isEnrolled && c.sections.isNotEmpty()) {
                    item {
                        Text("Curriculum", fontWeight = FontWeight.Bold, color = LmsColors.Indigo900,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    }
                    items(c.sections) { section ->
                        var expanded by remember { mutableStateOf(false) }
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
                            .background(LmsColors.Indigo50.copy(0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, LmsColors.Indigo900.copy(0.05f), RoundedCornerShape(12.dp))
                            .clickable { expanded = !expanded }
                            .padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(section.title, fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                                    Text("${section.lessonCount ?: section.lessons.size} lessons • ${section.duration ?: ""}",
                                        style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                                }
                                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null, tint = LmsColors.Indigo900.copy(0.4f))
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    section.lessons.forEach { lesson ->
                                        var lessonQuizzes by remember { mutableStateOf<List<QuizDetailDTO>>(emptyList()) }
                                        LaunchedEffect(lesson.lessonId) {
                                            try {
                                                val res = NetworkClient.apiService.getLesson(lesson.lessonId)
                                                if (res.isSuccessful) lessonQuizzes = res.body()?.quizz ?: emptyList()
                                            } catch (_: Exception) {}
                                        }

                                        Column {
                                            Row(modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (activeLesson?.lessonId == lesson.lessonId) LmsColors.Indigo50 else Color.Transparent)
                                                .clickable { 
                                                    activeLesson = lesson
                                                    isViewingVideo = true
                                                }
                                                .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (activeLesson?.lessonId == lesson.lessonId) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle,
                                                    contentDescription = null,
                                                    tint = if (activeLesson?.lessonId == lesson.lessonId) LmsColors.Indigo600 else LmsColors.Indigo600.copy(0.6f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        lesson.title,
                                                        color = LmsColors.Indigo900,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (activeLesson?.lessonId == lesson.lessonId) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }

                                            // Quizzes for this lesson
                                            lessonQuizzes.forEach { quiz ->
                                                var showQuiz by remember { mutableStateOf(false) }
                                                Row(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 32.dp, top = 2.dp, bottom = 2.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(LmsColors.Amber500.copy(0.1f))
                                                    .clickable { showQuiz = true }
                                                    .padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Quiz, null, tint = LmsColors.Amber500, modifier = Modifier.size(20.dp))
                                                    Spacer(Modifier.width(12.dp))
                                                    Text(quiz.title, style = MaterialTheme.typography.bodySmall, color = LmsColors.Indigo900)
                                                }

                                                if (showQuiz) {
                                                    QuizTakingScreen(quiz = quiz, onDismiss = { showQuiz = false })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Review Dialog
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Leave a Review") },
            text = {
                Column {
                    Text("Rating")
                    Row {
                        repeat(5) { i ->
                            IconButton(onClick = { rating = i + 1 }) {
                                Icon(if (i < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    null, tint = LmsColors.Amber500)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Review") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            NetworkClient.apiService.addReview(
                                courseId,
                                CourseReviewCreateDTO(
                                    reviewText = reviewText,   // ← reviewText NOT comment
                                    rating = rating
                                )
                            )
                            snackbar.showSnackbar("Review submitted!")
                        } catch (_: Exception) {}
                        showReviewDialog = false
                    }
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") } }
        )
    }
}

// ── My Learning ───────────────────────────────────────────────────────────────
@Composable
fun MyLearningScreen(onCourseClick: (Long) -> Unit) {
    var enrollments by remember { mutableStateOf<List<EnrollmentResponseDTO>>(emptyList()) }
    var loading     by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val res = NetworkClient.apiService.getMyEnrollments()
            if (res.isSuccessful) enrollments = res.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(LmsColors.Indigo600, Color.White)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "My Learning",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LmsColors.Indigo900
                )
                Text(
                    text = "${enrollments.size} enrolled courses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LmsColors.Indigo900.copy(alpha = 0.7f)
                )
            }
        }

        if (loading) { LoadingIndicator() } else {
            if (enrollments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.School, null, tint = LmsColors.Indigo900.copy(0.1f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("You haven't enrolled in any courses yet", color = LmsColors.Subtitle)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
                    items(enrollments) { enrollment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clickable { enrollment.courseId?.let { onCourseClick(it) } },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(60.dp).background(LmsColors.Indigo50, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.MenuBook, null, tint = LmsColors.Indigo600)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        enrollment.courseTitle ?: "Course",
                                        fontWeight = FontWeight.Bold,
                                        color = LmsColors.Indigo900,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        enrollment.instructor ?: "Unknown Instructor",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LmsColors.Subtitle
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    val statusColor = when (enrollment.status?.uppercase()) {
                                        "ACTIVE"    -> LmsColors.Teal500
                                        "COMPLETED" -> LmsColors.Indigo600
                                        "CANCELLED" -> LmsColors.Error
                                        else        -> Color.Gray
                                    }
                                    Text(
                                        enrollment.status ?: "PAID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, null, tint = LmsColors.Indigo900.copy(0.2f))
                            }
                        }
                    }
                }
            }
        }
    }
}



// ── Student Profile ────────────────────────────────────────────────────────────
@Composable
fun StudentProfileScreen(onLogout: () -> Unit) {
    var payments by remember { mutableStateOf<List<PaymentResponseDTO>>(emptyList()) }
    var loading  by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val res = NetworkClient.apiService.getMyPayments()
            if (res.isSuccessful) payments = res.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    val email   = NetworkClient.getEmail() ?: ""
    val role    = NetworkClient.getRole() ?: "USER"
    val initial = email.firstOrNull()?.uppercase() ?: "S"

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(LmsColors.Indigo600, Color.White)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LmsColors.Indigo900
                )
            }
            Spacer(Modifier.height(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape, 
                    color = LmsColors.Indigo600,
                    modifier = Modifier.size(100.dp),
                    border = BorderStroke(2.dp, LmsColors.Indigo900.copy(0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(initial, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = LmsColors.Indigo900)
                Spacer(Modifier.height(8.dp))
                RoleBadge(role)
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
            Text("Payment History", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = LmsColors.Indigo900,
                modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
        }

        if (loading) {
            item { LoadingIndicator() }
        } else if (payments.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Receipt, null, tint = LmsColors.Indigo900.copy(0.1f), modifier = Modifier.size(60.dp))
                    Text("No payments yet", color = LmsColors.Subtitle)
                }
            }
        } else {
            items(payments) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).background(LmsColors.Indigo50, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Receipt, null, tint = LmsColors.Indigo600)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(payment.courseTitle ?: "Course", fontWeight = FontWeight.Bold, color = LmsColors.Indigo900)
                            Text(payment.createdAt ?: "",
                                style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${"$%.2f".format(payment.amount ?: 0.0)}",
                                fontWeight = FontWeight.Bold, color = LmsColors.Indigo600)
                            val sc = when (payment.status?.uppercase()) {
                                "PAID"      -> LmsColors.Teal500
                                "PENDING"   -> LmsColors.Teal500
                                "FAILED"    -> LmsColors.Error
                                else        -> Color.Gray
                            }
                            Text(payment.status ?: "",
                                style = MaterialTheme.typography.labelSmall, color = sc, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { NetworkClient.clearSession(); onLogout() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LmsColors.Error),
                border = BorderStroke(1.dp, LmsColors.Error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}


