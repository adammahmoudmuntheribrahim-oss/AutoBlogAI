package com.autoblog.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.autoblog.ai.data.api.*
import com.autoblog.ai.utils.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    var geminiKey by remember { mutableStateOf(prefs.getGeminiApiKey()) }
    var clientId by remember { mutableStateOf(prefs.getBloggerClientId()) }
    var clientSecret by remember { mutableStateOf(prefs.getBloggerClientSecret()) }
    var refreshToken by remember { mutableStateOf(prefs.getBloggerRefreshToken()) }
    var blogId by remember { mutableStateOf(prefs.getBloggerBlogId()) }
    var pexelsKey by remember { mutableStateOf(prefs.getPexelsApiKey()) }

    var geminiTestResult by remember { mutableStateOf<Boolean?>(null) }
    var bloggerTestResult by remember { mutableStateOf<Boolean?>(null) }
    var pexelsTestResult by remember { mutableStateOf<Boolean?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text("⚙️ إعدادات API", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // ---------- Gemini ----------
        OutlinedTextField(
            value = geminiKey,
            onValueChange = { geminiKey = it; geminiTestResult = null },
            label = { Text("مفتاح Gemini API") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    scope.launch {
                        prefs.setGeminiApiKey(geminiKey)
                        geminiTestResult = GeminiService(prefs).testApiKey(geminiKey)
                    }
                }
            ) { Text("اختبار Gemini") }
            if (geminiTestResult != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (geminiTestResult == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (geminiTestResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    if (geminiTestResult == true) "صالحة" else "خاطئة",
                    color = if (geminiTestResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------- Blogger OAuth ----------
        Text("🔑 بيانات Blogger OAuth 2.0", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it; bloggerTestResult = null },
            label = { Text("معرف العميل (Client ID)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = clientSecret,
            onValueChange = { clientSecret = it; bloggerTestResult = null },
            label = { Text("كلمة سر العميل (Client Secret)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = refreshToken,
            onValueChange = { refreshToken = it; bloggerTestResult = null },
            label = { Text("رمز التحديث (Refresh Token)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = blogId,
            onValueChange = { blogId = it; bloggerTestResult = null },
            label = { Text("معرف المدونة (Blog ID)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    scope.launch {
                        prefs.setBloggerClientId(clientId)
                        prefs.setBloggerClientSecret(clientSecret)
                        prefs.setBloggerRefreshToken(refreshToken)
                        prefs.setBloggerBlogId(blogId)
                        bloggerTestResult = BloggerRepository(prefs).testConnection()
                    }
                }
            ) { Text("اختبار OAuth") }
            if (bloggerTestResult != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (bloggerTestResult == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (bloggerTestResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    if (bloggerTestResult == true) "صالحة" else "خاطئة",
                    color = if (bloggerTestResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---------- Pexels ----------
        OutlinedTextField(
            value = pexelsKey,
            onValueChange = { pexelsKey = it; pexelsTestResult = null },
            label = { Text("مفتاح Pexels API") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    scope.launch {
                        prefs.setPexelsApiKey(pexelsKey)
                        pexelsTestResult = AiImageRepository(prefs).testConnection()
                    }
                }
            ) { Text("اختبار Pexels") }
            if (pexelsTestResult != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (pexelsTestResult == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (pexelsTestResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    if (pexelsTestResult == true) "صالحة" else "خاطئة",
                    color = if (pexelsTestResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                prefs.setGeminiApiKey(geminiKey)
                prefs.setBloggerClientId(clientId)
                prefs.setBloggerClientSecret(clientSecret)
                prefs.setBloggerRefreshToken(refreshToken)
                prefs.setBloggerBlogId(blogId)
                prefs.setPexelsApiKey(pexelsKey)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 حفظ جميع الإعدادات")
        }
    }
}
