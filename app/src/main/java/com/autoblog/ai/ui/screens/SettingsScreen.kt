package com.autoblog.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.autoblog.ai.utils.PreferencesManager
import com.autoblog.ai.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    
    var geminiKey by remember { mutableStateOf(prefs.getGeminiApiKey()) }
    var pexelsKey by remember { mutableStateOf(prefs.getPexelsApiKey()) }
    var blogId by remember { mutableStateOf(prefs.getBloggerBlogId()) }
    var clientId by remember { mutableStateOf(prefs.getBloggerClientId()) }
    var clientSecret by remember { mutableStateOf(prefs.getBloggerClientSecret()) }
    var refreshToken by remember { mutableStateOf(prefs.getBloggerRefreshToken()) }
    var rssUrl by remember { mutableStateOf(prefs.getRssUrl()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("الإعدادات", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection("مفاتيح الذكاء الاصطناعي", Icons.Default.Psychology) {
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Gemini API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = pexelsKey,
                    onValueChange = { pexelsKey = it },
                    label = { Text("Pexels API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            SettingsSection("إعدادات Blogger", Icons.Default.RssFeed) {
                OutlinedTextField(
                    value = blogId,
                    onValueChange = { blogId = it },
                    label = { Text("Blog ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    label = { Text("Client ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientSecret,
                    onValueChange = { clientSecret = it },
                    label = { Text("Client Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = refreshToken,
                    onValueChange = { refreshToken = it },
                    label = { Text("Refresh Token") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            SettingsSection("مصادر المحتوى", Icons.Default.Link) {
                OutlinedTextField(
                    value = rssUrl,
                    onValueChange = { rssUrl = it },
                    label = { Text("RSS Feed URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.com/feed") }
                )
            }

            Button(
                onClick = {
                    prefs.setGeminiApiKey(geminiKey)
                    prefs.setPexelsApiKey(pexelsKey)
                    prefs.setBloggerBlogId(blogId)
                    prefs.setBloggerClientId(clientId)
                    prefs.setBloggerClientSecret(clientSecret)
                    prefs.setBloggerRefreshToken(refreshToken)
                    prefs.setRssUrl(rssUrl)
                    viewModel.updateSetupStatus()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("حفظ الإعدادات")
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
            content()
        }
    }
}
