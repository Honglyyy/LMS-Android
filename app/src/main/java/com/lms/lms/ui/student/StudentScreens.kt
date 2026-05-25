package com.lms.lms.ui.student

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    if (selectedCourseId != null) {
        CourseDetailScreen(courseId = selectedCourseId!!, onBack = { selectedCourseId = null })
        return
    }

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
                        onClick  = { currentTab = tab },
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
                StudentTab.HOME        -> StudentHomeScreen(onCourseClick = { selectedCourseId = it })
                StudentTab.BROWSE      -> BrowseCoursesScreen(onCourseClick = { selectedCourseId = it })
                StudentTab.MY_LEARNING -> MyLearningScreen(onCourseClick = { selectedCourseId = it })
                StudentTab.PROFILE     -> StudentProfileScreen(onLogout = onLogout)
            }
        }
    }
}

// ── Home Screen ───────────────────────────────────────────────────────────────
@Composable
fun StudentHomeScreen(onCourseClick: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    var courses       by remember { mutableStateOf<List<CourseResponseDTO>>(emptyList()) }
    var categories    by remember { mutableStateOf<List<CategoryResponseDTO>>(emptyList()) }
    var myEnrollments by remember { mutableStateOf<List<EnrollmentResponseDTO>>(emptyList()) }
    var loading       by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        try {
            val cr = NetworkClient.apiService.getAllCourses()
            val ca = NetworkClient.apiService.getAllCategories()
            val me = NetworkClient.apiService.getMyEnrollments()
            if (cr.isSuccessful) courses       = cr.body() ?: emptyList()
            if (ca.isSuccessful) categories    = ca.body() ?: emptyList()
            if (me.isSuccessful) myEnrollments = me.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    val email     = NetworkClient.getEmail() ?: "Student"
    val firstName = email.substringBefore("@").replaceFirstChar { it.uppercase() }

    // Filter by selected category name
    val filteredCourses = if (selectedCategory == null) courses
    else {
        val catName = categories.find { it.categoryId == selectedCategory }?.category
        courses.filter { it.categories.contains(catName) }
    }

    if (loading) { LoadingIndicator(); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(LmsColors.Surface),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(LmsColors.Indigo900, LmsColors.Indigo600)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column {
                    Text("Hello, $firstName 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("What do you want to learn today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(myEnrollments.size.toString(), fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp, color = Color.White)
                                Text("Enrolled", style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f))
                            }
                        }
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(courses.size.toString(), fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp, color = Color.White)
                                Text("Available", style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text("Categories", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                item {
                    FilterChip(selected = selectedCategory == null,
                        onClick = { selectedCategory = null }, label = { Text("All") })
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.categoryId,
                        onClick  = { selectedCategory = if (selectedCategory == cat.categoryId) null else cat.categoryId },
                        label    = { Text(cat.category) }   // ← cat.category NOT cat.name
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Featured Courses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${filteredCourses.size} courses", style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
            }
            Spacer(Modifier.height(12.dp))
        }

        items(filteredCourses) { course ->
            val enrolled = myEnrollments.any { it.courseId == course.courseId }
            CourseCard(
                title      = course.title,
                instructor = course.instructor,                  // ← instructor NOT instructorName
                category   = course.categories.firstOrNull(),   // ← categories list
                price      = course.price,
                imageUrl   = course.coverDir,                   // ← coverDir NOT coverImageUrl
                badge      = if (enrolled) "Enrolled" else null,
                onClick    = { onCourseClick(course.courseId) }, // ← courseId NOT id
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        if (filteredCourses.isEmpty()) {
            item { EmptyState("No courses available", Icons.Outlined.MenuBook) }
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

    Column(modifier = Modifier.fillMaxSize().background(LmsColors.Surface)) {
        GradientHeader(title = "Browse Courses", subtitle = "${courses.size} courses available")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search courses, instructors...") },
            leadingIcon  = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = if (search.isNotBlank()) {
                { IconButton(onClick = { search = "" }) { Icon(Icons.Filled.Clear, null) } }
            } else null,
            shape = RoundedCornerShape(14.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = LmsColors.Indigo600,
                unfocusedBorderColor = LmsColors.Indigo200
            )
        )
        Spacer(Modifier.height(8.dp))
        if (loading) { LoadingIndicator() } else {
            LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 20.dp)) {
                items(filtered) { course ->
                    CourseCard(
                        title      = course.title,
                        instructor = course.instructor,
                        category   = course.categories.firstOrNull(),
                        price      = course.price,
                        imageUrl   = course.coverDir,
                        onClick    = { onCourseClick(course.courseId) },
                        modifier   = Modifier.padding(vertical = 6.dp)
                    )
                }
                if (filtered.isEmpty()) {
                    item { EmptyState("No results for \"$search\"", Icons.Outlined.SearchOff) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.title ?: "Course", maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LmsColors.Indigo900,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            course?.let { c ->
                Surface(shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (isEnrolled) {
                            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                                color = LmsColors.Success.copy(alpha = 0.12f)) {
                                Row(modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = LmsColors.Success)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Enrolled", color = LmsColors.Success, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(onClick = { showReviewDialog = true }, shape = RoundedCornerShape(14.dp)) {
                                Icon(Icons.Filled.Star, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Review")
                            }
                        } else {
                            PrimaryButton(
                                text = if ((c.price ?: 0.0) > 0)
                                    "Enroll  •  ${"$%.2f".format(c.price)}" else "Enroll Free",
                                onClick = {
                                    scope.launch {
                                        enrolling = true
                                        try {
                                            val res = NetworkClient.apiService.enroll(EnrollmentCreateDTO(courseId))
                                            if (res.isSuccessful) {
                                                myEnrollments = myEnrollments + res.body()!!
                                                snackbar.showSnackbar("Successfully enrolled!")
                                            } else {
                                                snackbar.showSnackbar("Enrollment failed")
                                            }
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Error: ${e.message}")
                                        }
                                        enrolling = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                loading  = enrolling
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
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 20.dp)) {

                // Header
                item {
                    Column(modifier = Modifier.fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(LmsColors.Indigo800, LmsColors.Indigo600)))
                        .padding(20.dp)) {
                        // Categories
                        if (c.categories.isNotEmpty()) {
                            Text(c.categories.joinToString(" · ").uppercase(),
                                color = LmsColors.Amber400,
                                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(c.title, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        c.instructor?.let {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, null,
                                    tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("by $it", color = Color.White.copy(0.8f),
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Layers, null,
                                    tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${c.sectionCount ?: c.sections.size} sections",
                                    color = Color.White.copy(0.8f),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            c.rating?.let { r ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, null,
                                        tint = LmsColors.Amber400, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${"%.1f".format(r)}",
                                        color = Color.White.copy(0.8f),
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            c.overallDuration?.let { dur ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Timer, null,
                                        tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(dur, color = Color.White.copy(0.8f),
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Description
                c.description?.let { desc ->
                    item {
                        Card(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(14.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("About this course", fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Text(desc, style = MaterialTheme.typography.bodyMedium, color = LmsColors.Subtitle)
                            }
                        }
                    }
                }

                // Curriculum
                if (c.sections.isNotEmpty()) {
                    item {
                        Text("Curriculum", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                    }
                    items(c.sections) { section ->
                        var expanded by remember { mutableStateOf(false) }
                        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth()
                                    .clickable { expanded = !expanded }.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Folder, null,
                                            tint = LmsColors.Indigo600, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(section.title, fontWeight = FontWeight.SemiBold)
                                            Text("${section.lessonCount ?: section.lessons.size} lessons",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = LmsColors.Subtitle)
                                        }
                                    }
                                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        null, tint = LmsColors.Subtitle)
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                                        section.lessons.forEachIndexed { idx, lesson ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(32.dp)
                                                    .background(LmsColors.Indigo50, RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        if (lesson.videoDir != null) Icons.Filled.PlayArrow else Icons.Filled.Article,
                                                        null, tint = LmsColors.Indigo600,
                                                        modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Text(lesson.title, style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f))
                                                if (!isEnrolled) {
                                                    Icon(Icons.Filled.Lock, null,
                                                        tint = LmsColors.Subtitle, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            if (idx < section.lessons.lastIndex)
                                                HorizontalDivider(color = LmsColors.Indigo50)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Reviews (embedded in CourseDetailDTO)
                if (c.reviews.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Reviews", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(8.dp))
                    }
                    items(c.reviews.take(5)) { review ->
                        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(review.username ?: "Student",   // ← username NOT userEmail
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f))
                                    Row {
                                        repeat(review.rating) {
                                            Icon(Icons.Filled.Star, null,
                                                tint = LmsColors.Amber500, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                review.reviewText?.let {       // ← reviewText NOT comment
                                    Spacer(Modifier.height(4.dp))
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
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

    Column(modifier = Modifier.fillMaxSize().background(LmsColors.Surface)) {
        GradientHeader("My Learning", "${enrollments.size} enrolled courses")
        if (loading) { LoadingIndicator() } else {
            if (enrollments.isEmpty()) {
                EmptyState("You haven't enrolled in any courses yet", Icons.Outlined.School)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 20.dp)) {
                    items(enrollments) { enrollment ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            .clickable { enrollment.courseId?.let { onCourseClick(it) } },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(56.dp).background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(LmsColors.Indigo600, LmsColors.Indigo400)),
                                    RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.MenuBook, null,
                                        tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(enrollment.courseTitle ?: "Course",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge)
                                    enrollment.instructor?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall,
                                            color = LmsColors.Subtitle)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    val statusColor = when (enrollment.status?.uppercase()) {
                                        "ACTIVE"    -> LmsColors.Success
                                        "COMPLETED" -> LmsColors.Indigo600
                                        "CANCELLED" -> LmsColors.Error
                                        else        -> LmsColors.Subtitle
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp),
                                        color = statusColor.copy(0.1f)) {
                                        Text(enrollment.status ?: "PENDING",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Icon(Icons.Filled.ChevronRight, null, tint = LmsColors.Subtitle)
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

    LazyColumn(modifier = Modifier.fillMaxSize().background(LmsColors.Surface),
        contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            GradientHeader("Profile")
            Spacer(Modifier.height(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(40.dp), color = LmsColors.Indigo600,
                    modifier = Modifier.size(80.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(initial, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(email, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                RoleBadge(role)
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Payment History", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
        }

        if (loading) {
            item { LoadingIndicator() }
        } else if (payments.isEmpty()) {
            item { EmptyState("No payments yet", Icons.Outlined.Receipt) }
        } else {
            items(payments) { payment ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).background(LmsColors.Indigo50, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Receipt, null, tint = LmsColors.Indigo600)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(payment.courseTitle ?: "Course", fontWeight = FontWeight.SemiBold)
                            Text(payment.username ?: payment.userEmail ?: "",   // ← username field
                                style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                            Text(payment.createdAt ?: "",
                                style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${"$%.2f".format(payment.amount ?: 0.0)}",
                                fontWeight = FontWeight.Bold, color = LmsColors.Indigo600)
                            val sc = when (payment.status?.uppercase()) {
                                "PAID"      -> LmsColors.Success
                                "PENDING"   -> LmsColors.Warning
                                "FAILED"    -> LmsColors.Error
                                else        -> LmsColors.Subtitle
                            }
                            Text(payment.status ?: "",
                                style = MaterialTheme.typography.labelSmall, color = sc)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { NetworkClient.clearSession(); onLogout() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LmsColors.Error),
                border = BorderStroke(1.dp, LmsColors.Error)
            ) {
                Icon(Icons.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
