package com.lms.lms.ui.instructor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lms.lms.ui.viewmodel.CourseManagementViewModel
import com.lms.lms.ui.viewmodel.InstructorViewModel
import com.lms.lms.ui.viewmodel.QuizViewModel
import com.lms.lms.ui.shared.*
import kotlinx.coroutines.launch

// ── Instructor Nav ─────────────────────────────────────────────────────────────
enum class InstructorTab { DASHBOARD, COURSES, PROFILE }

@Composable
fun InstructorApp(onLogout: () -> Unit) {
    val viewModel: InstructorViewModel = viewModel()
    var currentTab by remember { mutableStateOf(InstructorTab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(
                    InstructorTab.DASHBOARD to (Icons.Filled.Dashboard  to "Dashboard"),
                    InstructorTab.COURSES   to (Icons.AutoMirrored.Filled.MenuBook   to "My Courses"),
                    InstructorTab.PROFILE   to (Icons.Filled.Person     to "Profile")
                ).forEach { (tab, iconLabel) ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick  = { currentTab = tab },
                        icon     = { Icon(iconLabel.first, null) },
                        label    = { Text(iconLabel.second, maxLines = 1) },
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
                InstructorTab.DASHBOARD -> InstructorDashboard(viewModel)
                InstructorTab.COURSES   -> InstructorCoursesScreen(viewModel.categories)
                InstructorTab.PROFILE   -> InstructorProfileScreen(onLogout)
            }
        }
    }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
@Composable
fun InstructorDashboard(viewModel: InstructorViewModel) {
    val courses = viewModel.courses
    val enrollments = viewModel.enrollments
    val sections = viewModel.sections
    val lessons = viewModel.lessons
    val loading = viewModel.isLoading

    val email     = NetworkClient.getEmail() ?: "Instructor"
    val firstName = email.substringBefore("@").replaceFirstChar { it.uppercase() }

    if (loading) { LoadingIndicator(); return }

    LazyColumn(modifier = Modifier.fillMaxSize().background(LmsColors.Surface),
        contentPadding = PaddingValues(bottom = 20.dp)) {

        item {
            Box(modifier = Modifier.fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(LmsColors.Indigo900, LmsColors.Indigo600, LmsColors.Teal500)))
                .padding(20.dp)) {
                Column {
                    Text("Welcome back, $firstName 🎓",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Instructor Dashboard",
                        style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.75f))
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text("Overview", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Courses", courses.size.toString(), Icons.AutoMirrored.Filled.MenuBook,
                    LmsColors.Indigo600, modifier = Modifier.weight(1f))
                StatCard("Students", enrollments.size.toString(), Icons.Filled.People,
                    LmsColors.Teal500, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Sections", sections.size.toString(), Icons.Filled.Layers,
                    LmsColors.Indigo400, modifier = Modifier.weight(1f))
                StatCard("Lessons", lessons.size.toString(), Icons.Filled.PlayCircle,
                    LmsColors.Amber500, modifier = Modifier.weight(1f))
            }
        }

        if (courses.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Text("My Courses", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }
            items(courses.take(3)) { course ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))) {
                            if (!course.coverDir.isNullOrBlank()) {
                                AsyncImage(
                                    model = buildFullUrl(course.coverDir),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(LmsColors.Indigo50), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Outlined.MenuBook, null, tint = LmsColors.Indigo600)
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(course.title, fontWeight = FontWeight.SemiBold)
                            if (course.categories.isNotEmpty()) {
                                Text(course.categories.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                            }
                        }
                        course.price?.let {
                            Text("${"$%.0f".format(it)}", fontWeight = FontWeight.Bold, color = LmsColors.Indigo600)
                        }
                    }
                }
            }
        }

        if (enrollments.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Text("Recent Enrollments", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }
            items(enrollments.take(5)) { enrollment ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(20.dp), color = LmsColors.Indigo600) {
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                // ← username (not userEmail) is the display name
                                Text(enrollment.username?.firstOrNull()?.uppercase()
                                    ?: enrollment.userEmail?.firstOrNull()?.uppercase() ?: "S",
                                    color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(enrollment.username ?: enrollment.userEmail ?: "Student",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(enrollment.courseTitle ?: "Course",
                                style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                        }
                        val sc = when (enrollment.status?.uppercase()) {
                            "ACTIVE"    -> LmsColors.Success
                            "COMPLETED" -> LmsColors.Indigo600
                            "CANCELLED" -> LmsColors.Error
                            else        -> LmsColors.Subtitle
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = sc.copy(0.1f)) {
                            Text(enrollment.status ?: "PENDING",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = sc, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Instructor Courses ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorCoursesScreen(categories: List<CategoryResponseDTO>) {
    val viewModel: CourseManagementViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val courses = viewModel.courses
    val loading = viewModel.isLoading
    
    var showDialog  by remember { mutableStateOf(false) }
    var editCourse  by remember { mutableStateOf<CourseResponseDTO?>(null) }
    var managingCourse by remember { mutableStateOf<CourseResponseDTO?>(null) }
    val snackbar    = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadMyCourses() }

    if (managingCourse != null) {
        InstructorCurriculumManager(
            course = managingCourse!!,
            viewModel = viewModel,
            onBack = { managingCourse = null; viewModel.loadMyCourses() }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { editCourse = null; showDialog = true },
                    containerColor = LmsColors.Indigo600, contentColor = Color.White) {
                    Icon(Icons.Filled.Add, null)
                }
            },
            snackbarHost = { LmsSnackbarHost(snackbar) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
                GradientHeader("My Courses", "${courses.size} published")
                if (loading) { LoadingIndicator() } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 80.dp)) {
                        items(courses) { course ->
                            InstructorCourseItem(
                                course = course,
                                onEdit = { editCourse = course; showDialog = true },
                                onManage = { managingCourse = course },
                                onDelete = {
                                    viewModel.deleteCourse(course.courseId,
                                        onSuccess = { scope.launch { snackbar.showSnackbar("Course deleted") } },
                                        onError = { msg -> scope.launch { snackbar.showSnackbar(msg) } }
                                    )
                                }
                            )
                        }
                        if (courses.isEmpty()) {
                            item { EmptyState("No courses yet. Tap + to create one.", Icons.AutoMirrored.Outlined.MenuBook) }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CourseFormDialog(
            initial    = editCourse,
            categories = categories,
            onDismiss  = { showDialog = false },
            onSave     = { dto ->
                if (editCourse != null) {
                    viewModel.updateCourse(editCourse!!.courseId, dto,
                        onSuccess = { scope.launch { snackbar.showSnackbar("Course updated") }; showDialog = false },
                        onError = { msg -> scope.launch { snackbar.showSnackbar(msg) } }
                    )
                } else {
                    viewModel.createCourse(dto,
                        onSuccess = { scope.launch { snackbar.showSnackbar("Course created") }; showDialog = false },
                        onError = { msg -> scope.launch { snackbar.showSnackbar(msg) } }
                    )
                }
            }
        )
    }
}

@Composable
fun InstructorCourseItem(
    course: CourseResponseDTO,
    onEdit: () -> Unit,
    onManage: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))) {
                    if (!course.coverDir.isNullOrBlank()) {
                        AsyncImage(
                            model = buildFullUrl(course.coverDir),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(LmsColors.Indigo600, LmsColors.Indigo400))),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.title, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge)
                    if (course.categories.isNotEmpty()) {
                        Text(course.categories.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                    }
                }
                IconButton(onClick = onEdit)   { Icon(Icons.Filled.Edit,   null, tint = LmsColors.Indigo600) }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, null, tint = LmsColors.Error)
                }
            }
            
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable { onManage() }
                .background(LmsColors.Indigo50.copy(0.5f))
                .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = LmsColors.Indigo600, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Manage Curriculum", color = LmsColors.Indigo600, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Course") },
            text  = { Text("Delete \"${course.title}\"?") },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = LmsColors.Error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormDialog(
    initial: CourseResponseDTO?,
    categories: List<CategoryResponseDTO>,
    onDismiss: () -> Unit,
    onSave: (CourseCreateDTO) -> Unit
) {
    var title          by remember { mutableStateOf(initial?.title ?: "") }
    var description    by remember { mutableStateOf(initial?.description ?: "") }
    var price          by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var overallDuration by remember { mutableStateOf(initial?.overallDuration ?: "") }
    var coverDir       by remember { mutableStateOf(initial?.coverDir ?: "") }  // ← coverDir
    // Pre-select categories already on the course
    var selectedCatIds by remember {
        mutableStateOf<List<Long>>(initial?.categoryIds ?: emptyList())
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Course" else "Edit Course") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LmsTextField(value = title, onValueChange = { title = it }, label = "Course Title")
                LmsTextField(value = description, onValueChange = { description = it },
                    label = "Description", singleLine = false)
                LmsTextField(value = price, onValueChange = { price = it }, label = "Price (0 = free)")
                LmsTextField(value = overallDuration, onValueChange = { overallDuration = it },
                    label = "Duration (e.g. 10h 30m)")

                CourseCoverPicker(
                    currentUrl = coverDir.ifBlank { null },
                    onUploaded = { coverDir = it }
                )

                LmsTextField(value = coverDir, onValueChange = { coverDir = it },
                    label = "Cover Image URL (Manual)")

                // Multi-select categories
                Text("Categories", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                categories.forEach { cat ->
                    val checked = selectedCatIds.contains(cat.categoryId)
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        selectedCatIds = if (checked)
                            selectedCatIds - cat.categoryId
                        else
                            selectedCatIds + cat.categoryId
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checked, onCheckedChange = { on ->
                            selectedCatIds = if (on) selectedCatIds + cat.categoryId
                            else selectedCatIds - cat.categoryId
                        })
                        Text(cat.category)   // ← cat.category NOT cat.name
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(CourseCreateDTO(
                        title           = title,
                        description     = description.ifBlank { null },
                        price           = price.toDoubleOrNull(),
                        overallDuration = overallDuration.ifBlank { null },
                        coverDir        = coverDir.ifBlank { null },   // ← coverDir
                        instructor      = null,                        // server sets from auth token
                        categoryId      = selectedCatIds              // ← list of Long
                    ))
                },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Curriculum Management ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorCurriculumManager(
    course: CourseResponseDTO, 
    viewModel: CourseManagementViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val courseDetail = viewModel.courseDetail
    val loading = viewModel.isLoading
    val snackbar = remember { SnackbarHostState() }

    // Dialog states
    var showSectionDialog by remember { mutableStateOf(false) }
    var editSection by remember { mutableStateOf<SectionDetailDTO?>(null) }
    var showLessonDialog by remember { mutableStateOf(false) }
    var editLesson by remember { mutableStateOf<LessonDetailDTO?>(null) }
    var targetSectionId by remember { mutableStateOf<Long?>(null) }

    // Navigation states
    var managingQuizLesson by remember { mutableStateOf<LessonDetailDTO?>(null) }

    LaunchedEffect(course.courseId) { viewModel.loadCourseDetail(course.courseId) }

    if (managingQuizLesson != null) {
        InstructorQuizManager(
            lessonId = managingQuizLesson!!.lessonId,
            lessonTitle = managingQuizLesson!!.title,
            onBack = { 
                managingQuizLesson = null
                viewModel.loadCourseDetail(course.courseId)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(course.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    },
                    actions = {
                        TextButton(onClick = { editSection = null; showSectionDialog = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Section")
                        }
                    }
                )
            },
            snackbarHost = { LmsSnackbarHost(snackbar) }
        ) { padding ->
            if (loading) LoadingIndicator() else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
                    courseDetail?.sections?.forEach { section ->
                        item {
                            SectionHeaderItem(
                                section = section,
                                onEdit = { editSection = section; showSectionDialog = true },
                                onAddLesson = { targetSectionId = section.sectionId; editLesson = null; showLessonDialog = true },
                                onDelete = { viewModel.deleteSection(section.sectionId, course.courseId) }
                            )
                        }
                        items(section.lessons) { lesson ->
                            CurriculumLessonItem(
                                lesson = lesson,
                                onEdit = { 
                                    targetSectionId = section.sectionId
                                    editLesson = lesson
                                    showLessonDialog = true 
                                },
                                onManageQuiz = { managingQuizLesson = lesson },
                                onDelete = { viewModel.deleteLesson(lesson.lessonId, course.courseId) }
                            )
                        }
                    }
                    if (courseDetail?.sections?.isEmpty() == true) {
                        item { EmptyState("No sections yet. Add one to start building your course.", Icons.Outlined.Layers) }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (showSectionDialog) {
            SectionFormDialog(
                initial = editSection?.let { SectionResponseDTO(it.sectionId, it.title, it.duration, course.courseId, null) },
                courseId = course.courseId,
                onDismiss = { showSectionDialog = false },
                onSave = { dto ->
                    if (editSection != null) viewModel.updateSection(editSection!!.sectionId, dto, course.courseId)
                    else viewModel.createSection(dto, course.courseId)
                    showSectionDialog = false
                }
            )
        }

        if (showLessonDialog && targetSectionId != null) {
            LessonFormDialog(
                initial = editLesson?.let { LessonResponseDTO(it.lessonId, it.title, it.videoDir, targetSectionId!!, null) },
                sectionId = targetSectionId!!,
                onDismiss = { showLessonDialog = false },
                onSave = { dto ->
                    if (editLesson != null) viewModel.updateLesson(editLesson!!.lessonId, dto, course.courseId)
                    else viewModel.createLesson(dto, course.courseId)
                    showLessonDialog = false
                }
            )
        }
    }
}

@Composable
fun SectionHeaderItem(section: SectionDetailDTO, onEdit: () -> Unit, onDelete: () -> Unit, onAddLesson: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(LmsColors.Indigo50.copy(0.3f)).padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(section.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            section.duration?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle) }
        }
        IconButton(onClick = onAddLesson) { Icon(Icons.Default.AddCircleOutline, null, tint = LmsColors.Teal500) }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = LmsColors.Indigo600, modifier = Modifier.size(20.dp)) }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = LmsColors.Error, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun CurriculumLessonItem(lesson: LessonDetailDTO, onEdit: () -> Unit, onDelete: () -> Unit, onManageQuiz: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LmsColors.Indigo50)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (!lesson.videoDir.isNullOrBlank()) Icons.Default.PlayCircle else Icons.AutoMirrored.Filled.Article, 
                null, tint = if (!lesson.videoDir.isNullOrBlank()) LmsColors.Teal500 else LmsColors.Indigo600)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (!lesson.videoDir.isNullOrBlank()) {
                    Text(lesson.videoDir.substringAfterLast("/"), 
                        style = MaterialTheme.typography.labelSmall, color = LmsColors.Subtitle)
                }
            }
            IconButton(onClick = onManageQuiz) { Icon(Icons.Default.Quiz, null, tint = LmsColors.Amber500, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = LmsColors.Indigo600, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = LmsColors.Error, modifier = Modifier.size(20.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionFormDialog(
    initial: SectionResponseDTO?,
    courseId: Long,
    onDismiss: () -> Unit,
    onSave: (SectionCreateDTO) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var duration by remember { mutableStateOf(initial?.duration ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Section" else "Edit Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LmsTextField(value = title, onValueChange = { title = it }, label = "Section Title")
                LmsTextField(value = duration, onValueChange = { duration = it }, label = "Duration (e.g. 1h 30m)")
            }
        },
        confirmButton = {
            Button(onClick = { onSave(SectionCreateDTO(title, duration.ifBlank { null }, courseId)) }, enabled = title.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonFormDialog(
    initial: LessonResponseDTO?,
    sectionId: Long,
    onDismiss: () -> Unit,
    onSave: (LessonCreateDTO) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var videoDir by remember { mutableStateOf(initial?.videoDir ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Lesson" else "Edit Lesson") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LmsTextField(value = title, onValueChange = { title = it }, label = "Lesson Title")
                LessonVideoPicker(currentUrl = videoDir.ifBlank { null }, onUploaded = { videoDir = it })
                LmsTextField(value = videoDir, onValueChange = { videoDir = it }, label = "Video URL (Manual)")
            }
        },
        confirmButton = {
            Button(onClick = { onSave(LessonCreateDTO(title, videoDir.ifBlank { null }, sectionId)) }, enabled = title.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Instructor Profile ─────────────────────────────────────────────────────────
@Composable
fun InstructorProfileScreen(onLogout: () -> Unit) {
    val email   = NetworkClient.getEmail() ?: ""
    val role    = NetworkClient.getRole()  ?: "INSTRUCTOR"
    val initial = email.firstOrNull()?.uppercase() ?: "I"

    Column(modifier = Modifier.fillMaxSize().background(LmsColors.Surface)
        .verticalScroll(rememberScrollState())) {
        GradientHeader("Profile")
        Spacer(Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(shape = RoundedCornerShape(40.dp), color = LmsColors.Teal500,
                modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initial, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(email, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            RoleBadge(role)
        }
        Spacer(Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Account Info", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall, color = LmsColors.Subtitle)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Email, null, tint = LmsColors.Indigo600, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(email, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = LmsColors.Indigo50)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Badge, null, tint = LmsColors.Indigo600, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Role: $role", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = { NetworkClient.clearSession(); onLogout() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LmsColors.Error),
            border = BorderStroke(1.dp, LmsColors.Error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ── Quiz Management ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorQuizManager(lessonId: Long, lessonTitle: String, onBack: () -> Unit) {
    val viewModel: QuizViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val lessonDetail = viewModel.lessonDetail
    val loading = viewModel.isLoading
    
    var showQuizDialog by remember { mutableStateOf(false) }
    var editQuiz by remember { mutableStateOf<QuizDetailDTO?>(null) }
    var managingQuestionQuiz by remember { mutableStateOf<QuizDetailDTO?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(lessonId) { viewModel.loadQuizzes(lessonId) }

    if (managingQuestionQuiz != null) {
        InstructorQuestionManager(
            quizId = managingQuestionQuiz!!.quizId,
            quizTitle = managingQuestionQuiz!!.title,
            quizViewModel = viewModel,
            onBack = { 
                managingQuestionQuiz = null
                viewModel.loadQuizzes(lessonId)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quizzes: $lessonTitle") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { editQuiz = null; showQuizDialog = true },
                    containerColor = LmsColors.Indigo600, contentColor = Color.White) {
                    Icon(Icons.Default.Add, null)
                }
            },
            snackbarHost = { LmsSnackbarHost(snackbar) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
                if (loading) { LoadingIndicator() } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(lessonDetail?.quizz ?: emptyList()) { quiz ->
                            QuizItem(
                                quiz = quiz,
                                onEdit = { editQuiz = quiz; showQuizDialog = true },
                                onManageQuestions = { managingQuestionQuiz = quiz },
                                onDelete = { viewModel.deleteQuiz(quiz.quizId, lessonId) }
                            )
                        }
                        if (lessonDetail?.quizz?.isEmpty() == true) {
                            item { EmptyState("No quizzes for this lesson.", Icons.Outlined.Quiz) }
                        }
                    }
                }
            }
        }

        if (showQuizDialog) {
            QuizFormDialog(
                initial = editQuiz,
                lessonId = lessonId,
                onDismiss = { showQuizDialog = false },
                onSave = { dto ->
                    if (editQuiz != null) viewModel.updateQuiz(editQuiz!!.quizId, dto, lessonId)
                    else viewModel.createQuiz(dto, lessonId)
                    showQuizDialog = false
                }
            )
        }
    }
}

@Composable
fun QuizItem(quiz: QuizDetailDTO, onEdit: () -> Unit, onDelete: () -> Unit, onManageQuestions: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(LmsColors.Amber500.copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Quiz, null, tint = LmsColors.Amber500)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(quiz.title, fontWeight = FontWeight.SemiBold)
                Text("${quiz.question.size} Questions • ${quiz.totalPoints} Points", 
                    style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
            }
            IconButton(onClick = onManageQuestions) { Icon(Icons.AutoMirrored.Filled.List, null, tint = LmsColors.Indigo600) }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = LmsColors.Indigo600) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = LmsColors.Error) }
        }
    }
}

@Composable
fun QuizFormDialog(
    initial: QuizDetailDTO?,
    lessonId: Long,
    onDismiss: () -> Unit,
    onSave: (QuizCreateDTO) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var points by remember { mutableStateOf(initial?.totalPoints?.toString() ?: "100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Quiz" else "Edit Quiz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LmsTextField(value = title, onValueChange = { title = it }, label = "Quiz Title")
                LmsTextField(value = points, onValueChange = { points = it }, label = "Total Points")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(QuizCreateDTO(
                        title = title,
                        totalPoints = points.toDoubleOrNull() ?: 0.0,
                        lessonId = lessonId
                    ))
                },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorQuestionManager(
    quizId: Long, 
    quizTitle: String, 
    quizViewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val questions = quizViewModel.questions
    val loading = quizViewModel.isLoading
    
    var showQuestionDialog by remember { mutableStateOf(false) }
    var editQuestion by remember { mutableStateOf<QuestionDetailDTO?>(null) }
    var managingAnswerQuestion by remember { mutableStateOf<QuestionDetailDTO?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(quizId) { quizViewModel.loadQuestions(quizId) }

    if (managingAnswerQuestion != null) {
        InstructorAnswerManager(
            questionId = managingAnswerQuestion!!.questionId,
            questionText = managingAnswerQuestion!!.questionText,
            quizViewModel = quizViewModel,
            onBack = { 
                managingAnswerQuestion = null
                quizViewModel.loadQuestions(quizId)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Questions: $quizTitle") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { editQuestion = null; showQuestionDialog = true },
                    containerColor = LmsColors.Indigo600, contentColor = Color.White) {
                    Icon(Icons.Default.Add, null)
                }
            },
            snackbarHost = { LmsSnackbarHost(snackbar) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
                if (loading) LoadingIndicator() else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(questions) { question ->
                            QuestionItem(
                                question = question,
                                onEdit = { editQuestion = question; showQuestionDialog = true },
                                onManageAnswers = { managingAnswerQuestion = question },
                                onDelete = { quizViewModel.deleteQuestion(question.questionId, quizId) }
                            )
                        }
                        if (questions.isEmpty()) {
                            item { EmptyState("No questions yet.", Icons.AutoMirrored.Outlined.HelpOutline) }
                        }
                    }
                }
            }
        }

        if (showQuestionDialog) {
            QuestionFormDialog(
                initial = editQuestion,
                quizId = quizId,
                onDismiss = { showQuestionDialog = false },
                onSave = { dto ->
                    if (editQuestion != null) quizViewModel.updateQuestion(editQuestion!!.questionId, dto, quizId)
                    else quizViewModel.createQuestion(dto, quizId)
                    showQuestionDialog = false
                }
            )
        }
    }
}

@Composable
fun QuestionItem(question: QuestionDetailDTO, onEdit: () -> Unit, onDelete: () -> Unit, onManageAnswers: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(question.questionText, fontWeight = FontWeight.SemiBold)
            
            if (question.answers.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                question.answers.take(2).forEach { answer ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (answer.isCorrect) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            null,
                            tint = if (answer.isCorrect) LmsColors.Success else LmsColors.Subtitle.copy(0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(answer.answerText, style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle, maxLines = 1)
                    }
                }
                if (question.answers.size > 2) {
                    Text("... and ${question.answers.size - 2} more", 
                        style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle.copy(0.7f),
                        modifier = Modifier.padding(start = 22.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${question.answers.size} Answers", 
                    style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onManageAnswers) { Icon(Icons.Default.QuestionAnswer, null, tint = LmsColors.Indigo600) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = LmsColors.Indigo600) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = LmsColors.Error) }
            }
        }
    }
}

@Composable
fun QuestionFormDialog(
    initial: QuestionDetailDTO?,
    quizId: Long,
    onDismiss: () -> Unit,
    onSave: (QuestionCreateDTO) -> Unit
) {
    var text by remember { mutableStateOf(initial?.questionText ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Question" else "Edit Question") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LmsTextField(value = text, onValueChange = { text = it }, label = "Question Text", singleLine = false)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(QuestionCreateDTO(
                        questionText = text,
                        quizId = quizId
                    ))
                },
                enabled = text.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorAnswerManager(
    questionId: Long, 
    questionText: String, 
    quizViewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val answers = quizViewModel.answers
    val loading = quizViewModel.isLoading
    
    var showAnswerDialog by remember { mutableStateOf(false) }
    var editAnswer by remember { mutableStateOf<AnswerDetailDTO?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(questionId) { quizViewModel.loadAnswers(questionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Answers: $questionText", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editAnswer = null; showAnswerDialog = true },
                containerColor = LmsColors.Indigo600, contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        },
        snackbarHost = { LmsSnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
            if (loading) LoadingIndicator() else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(answers) { answer ->
                        AnswerItem(
                            answer = answer,
                            onEdit = { editAnswer = answer; showAnswerDialog = true },
                            onDelete = { quizViewModel.deleteAnswer(answer.answerId, questionId) }
                        )
                    }
                    if (answers.isEmpty()) {
                        item { EmptyState("No answers yet.", Icons.Outlined.Checklist) }
                    }
                }
            }
        }
    }

    if (showAnswerDialog) {
        AnswerFormDialog(
            initial = editAnswer,
            questionId = questionId,
            onDismiss = { showAnswerDialog = false },
            onSave = { dto ->
                if (editAnswer != null) quizViewModel.updateAnswer(editAnswer!!.answerId, dto, questionId)
                else quizViewModel.createAnswer(dto, questionId)
                showAnswerDialog = false
            }
        )
    }
}

@Composable
fun AnswerItem(answer: AnswerDetailDTO, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (answer.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                null,
                tint = if (answer.isCorrect) LmsColors.Success else LmsColors.Subtitle.copy(0.5f)
            )
            Spacer(Modifier.width(12.dp))
            Text(answer.answerText, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = LmsColors.Indigo600) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = LmsColors.Error) }
        }
    }
}

@Composable
fun AnswerFormDialog(
    initial: AnswerDetailDTO?,
    questionId: Long,
    onDismiss: () -> Unit,
    onSave: (AnswerCreateDTO) -> Unit
) {
    var text by remember { mutableStateOf(initial?.answerText ?: "") }
    var isCorrect by remember { mutableStateOf(initial?.isCorrect ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Answer" else "Edit Answer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LmsTextField(value = text, onValueChange = { text = it }, label = "Answer Text")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCorrect, onCheckedChange = { isCorrect = it })
                    Text("Correct Answer")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(AnswerCreateDTO(
                        answerText = text,
                        isCorrect = isCorrect,
                        questionId = questionId
                    ))
                },
                enabled = text.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

