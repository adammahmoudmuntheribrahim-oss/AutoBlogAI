package com.autoblog.ai.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.autoblog.ai.ui.components.ActivityItem
import com.autoblog.ai.ui.components.EnhancedStatCard
import com.autoblog.ai.ui.components.EngineControlCard
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
    
    var isEngineActive by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AutoBlog AI",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "لوحة تحكم إدارة المدونة الآلية",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerPublishWorker() },
                        enabled = !isLoading && isSetupComplete
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "تحديث",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // تنبيه الإعدادات
            if (!isSetupComplete) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "يرجى إكمال الإعدادات في صفحة الإعدادات لتفعيل الأتمتة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // قسم التحكم في محرك النشر
            item {
                EngineControlCard(
                    isActive = isEngineActive,
                    onToggle = { isEngineActive = !isEngineActive },
                    onActivate = { viewModel.triggerPublishWorker() }
                )
            }

            // عنوان الإحصائيات
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "نظرة عامة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "ملخص الأداء والإحصائيات الرئيسية",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // شبكة الإحصائيات
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // الصف الأول
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedStatCard(
                            label = "إجمالي المقالات",
                            value = "1.8k",
                            icon = Icons.Default.Article,
                            change = 12,
                            changeLabel = "هذا الشهر",
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedStatCard(
                            label = "منشور اليوم",
                            value = publishedToday.toString(),
                            icon = Icons.Default.Today,
                            change = 9,
                            changeLabel = "عن أمس",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // الصف الثاني
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedStatCard(
                            label = "إجمالي المشاهدات",
                            value = "284.5k",
                            icon = Icons.Default.Visibility,
                            change = 23,
                            changeLabel = "هذا الأسبوع",
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedStatCard(
                            label = "مجدولة",
                            value = queueCount.toString(),
                            icon = Icons.Default.Schedule,
                            change = 5,
                            changeLabel = "قيد الانتظار",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // الصف الثالث
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedStatCard(
                            label = "إيرادات اليوم",
                            value = "$12.84",
                            icon = Icons.Default.AttachMoney,
                            change = 5,
                            changeLabel = "عن أمس",
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedStatCard(
                            label = "إيرادات الشهر",
                            value = "$387.20",
                            icon = Icons.Default.TrendingUp,
                            change = 15,
                            changeLabel = "عن الشهر الماضي",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // الصف الرابع
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedStatCard(
                            label = "متوسط SEO",
                            value = "87/100",
                            icon = Icons.Default.Assessment,
                            change = 8,
                            changeLabel = "تحسن",
                            modifier = Modifier.weight(1f)
                        )
                        EnhancedStatCard(
                            label = "مصادر RSS",
                            value = "12",
                            icon = Icons.Default.Feed,
                            change = 2,
                            changeLabel = "مفعلة",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // قسم الأنشطة الأخيرة
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "الأنشطة الأخيرة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(
                listOf(
                    Triple("📝", "تم نشر مقالة جديدة", "قبل 5 دقائق"),
                    Triple("📅", "تم جدولة 3 مقالات", "قبل 15 دقيقة"),
                    Triple("🔄", "تم تحديث إعدادات RSS", "قبل ساعة")
                )
            ) { (icon, action, time) ->
                ActivityItem(
                    icon = icon,
                    action = action,
                    time = time
                )
            }

            // قسم المقالات الأخيرة
            if (posts.isNotEmpty()) {
                item {
                    Text(
                        "المقالات الأخيرة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(posts) { article ->
                    ArticleItem(article)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadge(article.status)
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(article.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (article.status == PostStatus.FAILED && article.failureReason != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "خطأ: ${article.failureReason}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: PostStatus) {
    val (text, color) = when (status) {
        PostStatus.PENDING -> "في الانتظار" to Color(0xFF9CA3AF)
        PostStatus.PROCESSING -> "جاري المعالجة" to MaterialTheme.colorScheme.primary
        PostStatus.FAILED -> "فشل" to MaterialTheme.colorScheme.error
        PostStatus.PUBLISHED -> "تم النشر" to Color(0xFF10B981)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
