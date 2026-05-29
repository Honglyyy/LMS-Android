package com.lms.lms.ui.shared

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lms.lms.data.api.NetworkClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// ── Upload state ──────────────────────────────────────────────────────────────
sealed class UploadState {
    object Idle      : UploadState()
    object Picking   : UploadState()
    object Uploading : UploadState()
    data class Success(val url: String, val fileName: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

// ── Course Cover Picker ────────────────────────────────────────────────────────
/**
 * Shows a tappable cover image area. On tap opens the image picker,
 * uploads to /api/uploads/course-cover, and calls onUploaded(url).
 */
@Composable
fun CourseCoverPicker(
    currentUrl: String?,
    onUploaded: (url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var state   by remember { mutableStateOf<UploadState>(
        if (currentUrl != null) UploadState.Success(currentUrl, "") else UploadState.Idle
    ) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            state = UploadState.Uploading
            try {
                val part = uri.toMultipartPart(context, "file")
                val res  = NetworkClient.apiService.uploadCourseCover(part)
                if (res.isSuccessful && res.body() != null) {
                    val uploadedUrl = res.body()!!.url
                    state = UploadState.Success(uploadedUrl, res.body()!!.fileName)
                    onUploaded(uploadedUrl)
                } else {
                    state = UploadState.Error("Upload failed (${res.code()})")
                }
            } catch (e: Exception) {
                state = UploadState.Error(e.message ?: "Upload error")
            }
        }
    }

    Column(modifier = modifier) {
        Text("Course Cover Image",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = LmsColors.OnSurface)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.5.dp,
                    color = if (state is UploadState.Error) LmsColors.Error else LmsColors.Indigo200,
                    shape = RoundedCornerShape(14.dp)
                )
                .background(LmsColors.Indigo50)
                .clickable(enabled = state !is UploadState.Uploading) {
                    launcher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is UploadState.Uploading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LmsColors.Indigo600, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Uploading...", style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                    }
                }
                is UploadState.Success -> {
                    // Show preview
                    val fullUrl = buildFullUrl(s.url)
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Change overlay
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Text("Tap to change", color = Color.White,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                is UploadState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Outlined.BrokenImage, null,
                            tint = LmsColors.Error, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(s.message, color = LmsColors.Error,
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap to retry", color = LmsColors.Subtitle,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.AddPhotoAlternate, null,
                            tint = LmsColors.Indigo400, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to upload cover image",
                            style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                        Text("JPG, PNG, WEBP, GIF • max 5 MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = LmsColors.Subtitle.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Success chip showing filename
        AnimatedVisibility(visible = state is UploadState.Success) {
            val s = state as? UploadState.Success
            if (s != null && s.fileName.isNotBlank()) {
                Surface(
                    modifier = Modifier.padding(top = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = LmsColors.Success.copy(alpha = 0.1f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null,
                            tint = LmsColors.Success, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.fileName, color = LmsColors.Success,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ── Lesson Video Picker ────────────────────────────────────────────────────────
/**
 * Shows a tappable video upload area. On tap opens the video picker,
 * uploads to /api/uploads/lesson-video, and calls onUploaded(url).
 */
@Composable
fun LessonVideoPicker(
    currentUrl: String?,
    onUploaded: (url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var state   by remember { mutableStateOf<UploadState>(
        if (currentUrl != null) UploadState.Success(currentUrl, currentUrl.substringAfterLast("/"))
        else UploadState.Idle
    ) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            state = UploadState.Uploading
            try {
                val part = uri.toMultipartPart(context, "file")
                val res  = NetworkClient.apiService.uploadLessonVideo(part)
                if (res.isSuccessful && res.body() != null) {
                    val uploadedUrl = res.body()!!.url
                    state = UploadState.Success(uploadedUrl, res.body()!!.fileName)
                    onUploaded(uploadedUrl)
                } else {
                    state = UploadState.Error("Upload failed (${res.code()})")
                }
            } catch (e: Exception) {
                state = UploadState.Error(e.message ?: "Upload error")
            }
        }
    }

    Column(modifier = modifier) {
        Text("Lesson Video",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = LmsColors.OnSurface)
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.5.dp,
                    color = if (state is UploadState.Error) LmsColors.Error
                    else if (state is UploadState.Success) LmsColors.Teal500
                    else LmsColors.Indigo200,
                    shape = RoundedCornerShape(14.dp)
                )
                .background(
                    if (state is UploadState.Success) LmsColors.Teal500.copy(alpha = 0.05f)
                    else LmsColors.Indigo50
                )
                .clickable(enabled = state !is UploadState.Uploading) {
                    launcher.launch("video/*")
                }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is UploadState.Uploading -> {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = LmsColors.Teal500,
                            modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Column {
                            Text("Uploading video...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text("This may take a moment for large files",
                                style = MaterialTheme.typography.bodySmall,
                                color = LmsColors.Subtitle)
                        }
                    }
                }
                is UploadState.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(48.dp)
                            .background(LmsColors.Teal500.copy(0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.VideoFile, null,
                                tint = LmsColors.Teal500, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.fileName.ifBlank { "Video uploaded" },
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Tap to replace",
                                style = MaterialTheme.typography.bodySmall,
                                color = LmsColors.Subtitle)
                        }
                        Icon(Icons.Filled.CheckCircle, null,
                            tint = LmsColors.Success, modifier = Modifier.size(20.dp))
                    }
                }
                is UploadState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ErrorOutline, null,
                            tint = LmsColors.Error, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(s.message, color = LmsColors.Error,
                            style = MaterialTheme.typography.bodySmall)
                        Text("Tap to retry", color = LmsColors.Subtitle,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.VideoCall, null,
                            tint = LmsColors.Indigo400, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to upload lesson video",
                            style = MaterialTheme.typography.bodySmall, color = LmsColors.Subtitle)
                        Text("MP4, WEBM, MOV, AVI • max 500 MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = LmsColors.Subtitle.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

// ── Generic file picker row (for both types, reusable) ────────────────────────
@Composable
fun UploadOrUrlField(
    label: String,
    icon: ImageVector,
    urlValue: String,
    onUrlChange: (String) -> Unit,
    uploadedUrl: String?,
    onPickFile: () -> Unit,
    uploading: Boolean,
    acceptedFormats: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold)
        // Upload button row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onPickFile,
                enabled = !uploading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(icon, null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(if (uploading) "Uploading…" else "Choose file")
            }
            if (uploadedUrl != null) {
                Icon(Icons.Filled.CheckCircle, null,
                    tint = LmsColors.Success, modifier = Modifier.size(24.dp).align(Alignment.CenterVertically))
            }
        }
        Text(acceptedFormats, style = MaterialTheme.typography.labelSmall, color = LmsColors.Subtitle)
        // Manual URL fallback
        LmsTextField(
            value = if (uploadedUrl != null && urlValue.isBlank()) uploadedUrl else urlValue,
            onValueChange = onUrlChange,
            label = "…or paste URL directly"
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Converts a content Uri into a [MultipartBody.Part] with the correct MIME type. */
fun Uri.toMultipartPart(context: Context, partName: String): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(this) ?: "application/octet-stream"

    // Get the real file name from the content resolver
    val fileName = contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        if (nameIndex >= 0) cursor.getString(nameIndex) else "upload"
    } ?: "upload"

    val bytes = contentResolver.openInputStream(this)!!.use { it.readBytes() }
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, fileName, requestBody)
}

/** Prepend the base server URL if the path starts with /uploads/ */
fun buildFullUrl(path: String): String {
    if (path.isBlank()) return ""
    if (path.startsWith("http")) return path
    val base = "http://192.168.0.194:8080"   // Matched to NetworkClient.BASE_URL
    val normalizedPath = if (path.startsWith("/")) path else "/$path"
    return "$base$normalizedPath"
}