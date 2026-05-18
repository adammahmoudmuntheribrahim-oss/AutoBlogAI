package com.autoblog.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.data.model.PostStatus
import com.autoblog.ai.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSetupComplete by viewModel.isSetupComplete.collectAsState()
    
    val totalCount by viewModel.totalArticles.collectAsState(0)
    val publishedToday by viewModel.publishedToday.collectAsState(0)
    val failedCount by viewModel.failedArticles.collectAsState(0)
    val queueCount by viewModel.queueCount.collectAsState(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.triggerPublishWorker() }, enabled = !isLoading && isSetupComplete) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isSetupComplete) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text("يرجى إكمال الإعدادات في صفحة الإعدادات لتفعيل الأتمتة.")
                        }
                    }
                }
            }

            item {
                AnalyticsSection(totalCount, publishedToday, failedCount, queueCount)
            }

            item {
                Text("المقالات الأخيرة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(posts) { article ->
                ArticleItem(article)
            }
        }
    }
}

@Composable
fun AnalyticsSection(total: Int, today: Int, failed: Int, queue: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("الإجمالي", total.toString(), Icons.Default.Article, MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            StatCard("نشر اليوم", today.toString(), Icons.Default.Today, Color(0xFFC8E6C9), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("في الانتظار", queue.toString(), Icons.Default.HourglassEmpty, Color(0xFFFFF9C4), Modifier.weight(1f))
            StatCard("فشل", failed.toString(), Icons.Default.Error, MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, containerColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(article.status)
                Spacer(Modifier.weight(1f))
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(article.createdAt)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (article.status == PostStatus.FAILED && article.failureReason != null) {
                Spacer(Modifier.height(4.dp))
                Text("خطأ: ${article.failureReason}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatusBadge(status: PostStatus) {
    val (text, color) = when (status) {
        PostStatus.PENDING -> "في الانتظار" to Color.Gray
        PostStatus.PROCESSING -> "جاري المعالجة" to MaterialTheme.colorScheme.primary
        PostStatus.FAILED -> "فشل" to MaterialTheme.colorScheme.error
        PostStatus.PUBLISHED -> "تم النشر" to Color(0xFF4CAF50)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
