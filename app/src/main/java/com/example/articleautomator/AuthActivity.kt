package com.example.articleautomator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.articleautomator.auth.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authUrl = intent.getStringExtra("auth_url") ?: return finish()
        
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    
                    if (url.startsWith("com.example.articleautomator:/oauth2redirect") ||
                        url.startsWith("com.example.articleautomator:/pinterest-oauth")) {
                        
                        val uri = Uri.parse(url)
                        val state = uri.getQueryParameter("state")
                        val code = uri.getQueryParameter("code")
                        
                        val error = uri.getQueryParameter("error")
                        if (error != null) {
                            val result = Intent().apply { putExtra("error", error) }
                            setResult(RESULT_CANCELED, result)
                        } else if (tokenManager.verifyState(state)) {
                            val result = Intent().apply { 
                                putExtra("code", code)
                                val provider = if (url.contains("pinterest")) "pinterest" else "google"
                                putExtra("provider", provider)
                            }
                            setResult(RESULT_OK, result)
                        } else {
                            setResult(RESULT_CANCELED)
                        }
                        finish()
                        return true
                    }
                    return false
                }
            }
            loadUrl(authUrl)
        }
        setContentView(webView)
    }
}
