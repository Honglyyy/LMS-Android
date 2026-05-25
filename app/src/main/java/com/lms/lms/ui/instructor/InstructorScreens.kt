package com.lms.lms.ui.instructor

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lms.lms.data.api.NetworkClient
import com.lms.lms.data.model.*
import com.lms.lms.ui.shared.*
import kotlinx.coroutines.launch

// ── Instructor Nav ─────────────────────────────────────────────────────────────
enum class InstructorTab { DASHBOARD, COURSES, SECTIONS, LESSONS, PROFILE }

@Composable
fun InstructorApp(onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(InstructorTab.DASHBOARD) }
    var categories by remember { mutableStateOf<List<CategoryResponseDTO>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val res = NetworkClient.apiService.getAllCategories()
            if (res.isSuccessful) categories = res.body() ?: emptyList()
        } catch (_: Exception) {}
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(
                    InstructorTab.DASHBOARD to (Icons.Filled.Dashboard  to "Dashboard"),
                    InstructorTab.COURSES   to (Icons.Filled.MenuBook   to "Courses"),
                    InstructorTab.SECTIONS  to (Icons.Filled.Layers     to "Sections"),
                    InstructorTab.LESSONS   to (Icons.Filled.PlayCircle to "Lessons"),
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
                InstructorTab.DASHBOARD -> InstructorDashboard()
                InstructorTab.COURSES   -> InstructorCoursesScreen(categories)
                InstructorTab.SECTIONS  -> InstructorSectionsScreen()
                InstructorTab.LESSONS   -> InstructorLessonsScreen()
                InstructorTab.PROFILE   -> InstructorProfileScreen(onLogout)
            }
        }
    }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
@Composable
fun InstructorDashboard() {
    var courses     by remember { mutableStateOf<List<CourseResponseDTO>>(emptyList()) }
    var enrollments by remember { mutableStateOf<List<EnrollmentResponseDTO>>(emptyList()) }
    var sections    by remember { mutableStateOf<List<SectionResponseDTO>>(emptyList()) }
    var lessons     by remember { mutableStateOf<List<LessonResponseDTO>>(emptyList()) }
    var loading     by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val cr = NetworkClient.apiService.getMyCourses()
            val sc = NetworkClient.apiService.getMySections()
            val ls = NetworkClient.apiService.getMyLessons()
            if (cr.isSuccessful) courses  = cr.body() ?: emptyList()
            if (sc.isSuccessful) sections = sc.body() ?: emptyList()
            if (ls.isSuccessful) lessons  = ls.body() ?: emptyList()
            for (course in (cr.body() ?: emptyList())) {
                try {
                    val er = NetworkClient.apiService.getCourseEnrollments(course.courseId)
                    if (er.isSuccessful) enrollments = enrollments + (er.body() ?: emptyList())
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        loading = false
    }

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
                StatCard("Courses", courses.size.toString(), Icons.Filled.MenuBook,
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
                                    Icon(Icons.Outlined.MenuBook, null, tint = LmsColors.Indigo600)
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
    val scope = rememberCoroutineScope()
    var courses     by remember { mutableStateOf<List<CourseResponseDTO>>(emptyList()) }
    var loading     by remember { mutableStateOf(true) }
    var showDialog  by remember { mutableStateOf(false) }
    var editCourse  by remember { mutableStateOf<CourseResponseDTO?>(null) }
    val snackbar    = remember { SnackbarHostState() }

    fun load() { scope.launch {
        loading = true
        try {
            val res = NetworkClient.apiService.getMyCourses()
            if (res.isSuccessful) courses = res.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    } }
    LaunchedEffect(Unit) { load() }

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
                            onDelete = {
                                scope.launch {
                                    try {
                                        NetworkClient.apiService.deleteMyCourse(course.courseId)  // ← courseId
                                        snackbar.showSnackbar("Course deleted")
                                        load()
                                    } catch (_: Exception) { snackbar.showSnackbar("Failed to delete") }
                                }
                            }
                        )
                    }
                    if (courses.isEmpty()) {
                        item { EmptyState("No courses yet. Tap + to create one.", Icons.Outlined.MenuBook) }
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
                scope.launch {
                    try {
                        if (editCourse != null)
                            NetworkClient.apiService.updateMyCourse(editCourse!!.courseId, dto)  // ← courseId
                        else
                            NetworkClient.apiService.createMyCourse(dto)
                        snackbar.showSnackbar("Course saved!")
                        load()
                    } catch (_: Exception) { snackbar.showSnackbar("Failed to save") }
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun InstructorCourseItem(
    course: CourseResponseDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
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
                        Icon(Icons.Outlined.MenuBook, null, tint = Color.White, modifier = Modifier.size(26.dp))
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
                if ((course.price ?: 0.0) > 0) {
                    Text("${"$%.2f".format(course.price)}", color = LmsColors.Indigo600,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onEdit)   { Icon(Icons.Filled.Edit,   null, tint = LmsColors.Indigo600) }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Filled.Delete, null, tint = LmsColors.Error)
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

// ── Sections ──────────────────────────────────────────────────────────────────
@Composable
fun InstructorSectionsScreen() {
    val scope      = rememberCoroutineScope()
    var sections   by remember { mutableStateOf<List<SectionResponseDTO>>(emptyList()) }
    var courses    by remember { mutableStateOf<List<CourseResponseDTO>>(emptyList()) }
    var loading    by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editSection by remember { mutableStateOf<SectionResponseDTO?>(null) }
    val snackbar   = remember { SnackbarHostState() }

    fun load() { scope.launch {
        loading = true
        try {
            val sr = NetworkClient.apiService.getMySections()
            val cr = NetworkClient.apiService.getMyCourses()
            if (sr.isSuccessful) sections = sr.body() ?: emptyList()
            if (cr.isSuccessful) courses  = cr.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    } }
    LaunchedEffect(Unit) { load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editSection = null; showDialog = true },
                containerColor = LmsColors.Indigo600, contentColor = Color.White) {
                Icon(Icons.Filled.Add, null)
            }
        },
        snackbarHost = { LmsSnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
            GradientHeader("Sections", "${sections.size} sections")
            if (loading) { LoadingIndicator() } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 80.dp)) {
                    items(sections) { section ->
                        SectionItem(
                            section  = section,
                            onEdit   = { editSection = section; showDialog = true },
                            onDelete = {
                                scope.launch {
                                    try {
                                        NetworkClient.apiService.deleteMySection(section.sectionId) // ← sectionId
                                        snackbar.showSnackbar("Deleted")
                                        load()
                                    } catch (_: Exception) { snackbar.showSnackbar("Failed") }
                                }
                            }
                        )
                    }
                    if (sections.isEmpty()) {
                        item { EmptyState("No sections yet", Icons.Outlined.Layers) }
                    }
                }
            }
        }
    }

    if (showDialog) {
        SectionFormDialog(
            initial   = editSection,
            courses   = courses,
            onDismiss = { showDialog = false },
            onSave    = { dto ->
                scope.launch {
                    try {
                        if (editSection != null)
                            NetworkClient.apiService.updateMySection(editSection!!.sectionId, dto)
                        else
                            NetworkClient.apiService.createMySection(dto)
                        snackbar.showSnackbar("Section saved!")
                        load()
                    } catch (_: Exception) { snackbar.showSnackbar("Failed to save") }
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun SectionItem(section: SectionResponseDTO, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(LmsColors.Indigo50, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Folder, null, tint = LmsColors.Indigo600)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(section.title, fontWeight = FontWeight.SemiBold)
                // ← courseName NOT courseTitle
                section.courseName?.let {
                    Text("in: $it", style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                }
                section.duration?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = LmsColors.Subtitle)
                }
            }
            IconButton(onClick = onEdit)   { Icon(Icons.Filled.Edit,   null, tint = LmsColors.Indigo600) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = LmsColors.Error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionFormDialog(
    initial: SectionResponseDTO?,
    courses: List<CourseResponseDTO>,
    onDismiss: () -> Unit,
    onSave: (SectionCreateDTO) -> Unit
) {
    var title            by remember { mutableStateOf(initial?.title ?: "") }
    var duration         by remember { mutableStateOf(initial?.duration ?: "") }
    var selectedCourseId by remember { mutableStateOf(initial?.courseId ?: courses.firstOrNull()?.courseId) }
    var expanded         by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Section" else "Edit Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LmsTextField(value = title, onValueChange = { title = it }, label = "Section Title")
                LmsTextField(value = duration, onValueChange = { duration = it },
                    label = "Duration (optional)")
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = courses.find { it.courseId == selectedCourseId }?.title ?: "Select Course",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        courses.forEach { course ->
                            DropdownMenuItem(text = { Text(course.title) },
                                onClick = { selectedCourseId = course.courseId; expanded = false }) // ← courseId
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCourseId?.let {
                        onSave(SectionCreateDTO(
                            title    = title,
                            duration = duration.ifBlank { null },
                            courseId = it
                        ))
                    }
                },
                enabled = title.isNotBlank() && selectedCourseId != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Lessons ───────────────────────────────────────────────────────────────────
@Composable
fun InstructorLessonsScreen() {
    val scope      = rememberCoroutineScope()
    var lessons    by remember { mutableStateOf<List<LessonResponseDTO>>(emptyList()) }
    var sections   by remember { mutableStateOf<List<SectionResponseDTO>>(emptyList()) }
    var loading    by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editLesson by remember { mutableStateOf<LessonResponseDTO?>(null) }
    val snackbar   = remember { SnackbarHostState() }

    fun load() { scope.launch {
        loading = true
        try {
            val lr = NetworkClient.apiService.getMyLessons()
            val sr = NetworkClient.apiService.getMySections()
            if (lr.isSuccessful) lessons  = lr.body() ?: emptyList()
            if (sr.isSuccessful) sections = sr.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    } }
    LaunchedEffect(Unit) { load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editLesson = null; showDialog = true },
                containerColor = LmsColors.Indigo600, contentColor = Color.White) {
                Icon(Icons.Filled.Add, null)
            }
        },
        snackbarHost = { LmsSnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LmsColors.Surface)) {
            GradientHeader("Lessons", "${lessons.size} lessons")
            if (loading) { LoadingIndicator() } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 80.dp)) {
                    items(lessons) { lesson ->
                        LessonItem(
                            lesson   = lesson,
                            onEdit   = { editLesson = lesson; showDialog = true },
                            onDelete = {
                                scope.launch {
                                    try {
                                        NetworkClient.apiService.deleteMyLesson(lesson.lessonId) // ← lessonId
                                        snackbar.showSnackbar("Lesson deleted")
                                        load()
                                    } catch (_: Exception) { snackbar.showSnackbar("Failed") }
                                }
                            }
                        )
                    }
                    if (lessons.isEmpty()) {
                        item { EmptyState("No lessons yet", Icons.Outlined.PlayCircle) }
                    }
                }
            }
        }
    }

    if (showDialog) {
        LessonFormDialog(
            initial   = editLesson,
            sections  = sections,
            onDismiss = { showDialog = false },
            onSave    = { dto ->
                scope.launch {
                    try {
                        if (editLesson != null)
                            NetworkClient.apiService.updateMyLesson(editLesson!!.lessonId, dto) // ← lessonId
                        else
                            NetworkClient.apiService.createMyLesson(dto)
                        snackbar.showSnackbar("Lesson saved!")
                        load()
                    } catch (_: Exception) { snackbar.showSnackbar("Failed to save") }
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun LessonItem(lesson: LessonResponseDTO, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(
                if (lesson.videoDir != null) LmsColors.Teal500.copy(0.12f) else LmsColors.Indigo50,
                RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(
                    if (lesson.videoDir != null) Icons.Filled.PlayCircle else Icons.Filled.Article,
                    null,
                    tint = if (lesson.videoDir != null) LmsColors.Teal500 else LmsColors.Indigo600
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, fontWeight = FontWeight.SemiBold)
                // ← sectionName NOT sectionTitle
                lesson.sectionName?.let {
                    Text("in: $it", style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                }
                if (lesson.videoDir != null) {
                    Text("Video", style = MaterialTheme.typography.labelSmall, color = LmsColors.Teal500)
                }
            }
            IconButton(onClick = onEdit)   { Icon(Icons.Filled.Edit,   null, tint = LmsColors.Indigo600) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = LmsColors.Error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonFormDialog(
    initial: LessonResponseDTO?,
    sections: List<SectionResponseDTO>,
    onDismiss: () -> Unit,
    onSave: (LessonCreateDTO) -> Unit
) {
    var title            by remember { mutableStateOf(initial?.title ?: "") }
    var videoDir         by remember { mutableStateOf(initial?.videoDir ?: "") }  // ← videoDir
    var selectedSectionId by remember { mutableStateOf(initial?.sectionId ?: sections.firstOrNull()?.sectionId) }
    var expanded         by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Lesson" else "Edit Lesson") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LmsTextField(value = title, onValueChange = { title = it }, label = "Lesson Title")

                LessonVideoPicker(
                    currentUrl = videoDir.ifBlank { null },
                    onUploaded = { videoDir = it }
                )

                // NOTE: Java LessonCreateDTO has no "content" field — only title, videoDir, sectionId
                LmsTextField(value = videoDir, onValueChange = { videoDir = it },
                    label = "Video URL (Manual)")
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = sections.find { it.sectionId == selectedSectionId }?.title ?: "Select Section",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Section") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sections.forEach { section ->
                            DropdownMenuItem(text = { Text(section.title) },
                                onClick = { selectedSectionId = section.sectionId; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedSectionId?.let {
                        onSave(LessonCreateDTO(
                            title     = title,
                            videoDir  = videoDir.ifBlank { null },  // ← videoDir, no content
                            sectionId = it
                        ))
                    }
                },
                enabled = title.isNotBlank() && selectedSectionId != null
            ) { Text("Save") }
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
            Icon(Icons.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(40.dp))
    }
}
