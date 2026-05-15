package com.autoblog.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.utils.LocalStorage
import com.autoblog.ai.utils.PreferencesManager
import com.autoblog.ai.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    val localStorage = remember { LocalStorage(context) }
    val viewModel = remember { DashboardViewModel() }
    val posts by viewModel.posts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPosts(localStorage)
    }

    val allKeysSet = prefs.isAllKeysSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "مدونة الذكاء الآلي",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (!allKeysSet) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ يجب إدخال جميع مفاتيح Gemini وبيانات Blogger OAuth و Pexels للعمل التلقائي.",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 إحصائيات سريعة", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("عدد المقالات: ${posts.size}")
                Text("آخر تحديث: ${if (posts.isNotEmpty()) "الآن" else "لا يوجد"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* يمكن تشغيل العامل يدوياً */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = allKeysSet
        ) {
            Text(if (allKeysSet) "تشغيل النشر التلقائي الآن" else "الإعدادات غير مكتملة")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("قائمة المقالات", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(posts) { post ->
                PostCard(post)
            }
        }
    }
}

@Composable
fun PostCard(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = article.title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.content.take(100) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("الوسوم: ${article.tags}", style = MaterialTheme.typography.bodySmall)
            Text("الحالة: ${if (article.published) "منشور" else "مسودة"}")
        }
    }
}
