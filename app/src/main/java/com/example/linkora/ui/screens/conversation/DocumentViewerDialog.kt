package com.example.linkora.ui.screens.conversation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors
import java.io.File
import java.util.Locale

@Composable
fun DocumentViewerDialog(
    filePath: String,
    fileName: String,
    mimeType: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val glassColors = LocalGlassColors.current

    val file = remember(filePath) { File(filePath) }
    val isPdf = remember(fileName, mimeType) {
        mimeType?.contains("pdf", ignoreCase = true) == true || fileName.endsWith(".pdf", ignoreCase = true)
    }
    val isText = remember(fileName, mimeType) {
        mimeType?.startsWith("text/") == true ||
                fileName.endsWith(".txt", ignoreCase = true) ||
                fileName.endsWith(".md", ignoreCase = true) ||
                fileName.endsWith(".json", ignoreCase = true) ||
                fileName.endsWith(".kt", ignoreCase = true) ||
                fileName.endsWith(".java", ignoreCase = true) ||
                fileName.endsWith(".xml", ignoreCase = true)
    }

    val fileSizeFormatted = remember(file) {
        if (!file.exists()) "Unknown size"
        else {
            val length = file.length()
            if (length > 1024 * 1024) String.format(Locale.getDefault(), "%.1f MB", length / (1024f * 1024f))
            else String.format(Locale.getDefault(), "%d KB", length / 1024)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        GlassIconButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            onClick = onDismiss,
                            buttonSize = 38.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = fileName,
                                style = LinkoraTypography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "$fileSizeFormatted • ${if (isPdf) "PDF Document" else if (isText) "Plain Text / Code" else "Document"}",
                                style = LinkoraTypography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Open External
                        GlassIconButton(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Open External",
                            onClick = {
                                openExternalFile(context, file, mimeType)
                            },
                            buttonSize = 38.dp,
                            iconSize = 18.dp,
                            iconTint = LinkoraColors.Cyan
                        )
                    }
                }

                // Main Viewer Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!file.exists()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "File not found or still transferring via mesh",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    } else if (isPdf) {
                        PdfRendererView(file = file)
                    } else if (isText) {
                        TextViewerView(file = file)
                    } else {
                        GenericDocumentView(file = file, fileName = fileName, fileSize = fileSizeFormatted, mimeType = mimeType)
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfRendererView(file: File) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(file) {
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            fileDescriptor = pfd
            pdfRenderer = renderer
            totalPages = renderer.pageCount
            pageIndex = 0
        } catch (e: Exception) {
            loadError = "Unable to render PDF: ${e.message}"
        }

        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(pageIndex, pdfRenderer) {
        val renderer = pdfRenderer ?: return@LaunchedEffect
        if (totalPages > 0 && pageIndex in 0 until totalPages) {
            try {
                val page = renderer.openPage(pageIndex)
                // Render at high resolution
                val densityDpi = 2
                val width = page.width * densityDpi
                val height = page.height * densityDpi
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                currentBitmap = bitmap
            } catch (e: Exception) {
                loadError = "Page render error: ${e.message}"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loadError != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = loadError ?: "Error",
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${pageIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "Rendering page...",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                }
            }

            // PDF Navigation Controls
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (pageIndex > 0) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable(enabled = pageIndex > 0) { pageIndex-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous Page",
                        tint = if (pageIndex > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Page ${pageIndex + 1} of $totalPages",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (pageIndex < totalPages - 1) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable(enabled = pageIndex < totalPages - 1) { pageIndex++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next Page",
                        tint = if (pageIndex < totalPages - 1) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TextViewerView(file: File) {
    val content = remember(file) {
        try {
            file.readText().take(50000) // Read up to 50k chars
        } catch (e: Exception) {
            "Unable to read text file: ${e.message}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = content,
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun GenericDocumentView(
    file: File,
    fileName: String,
    fileSize: String,
    mimeType: String?
) {
    val context = LocalContext.current

    GlassCard(
        modifier = Modifier.padding(20.dp),
        shape = RoundedCornerShape(20.dp),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = fileName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "$fileSize • ${mimeType ?: "Binary File"}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LinkoraColors.Emerald)
                    .clickable {
                        openExternalFile(context, file, mimeType)
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Open in Supported App",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun openExternalFile(context: Context, file: File, mimeType: String?) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback: Share sheet
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
        } catch (_: Exception) {}
    }
}
