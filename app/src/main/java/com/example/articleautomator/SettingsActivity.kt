package com.example.articleautomator

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import com.example.articleautomator.model.WriterPersonality
import com.example.articleautomator.workflow.GeminiKeyManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject lateinit var geminiKeyManager: GeminiKeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        // Blogger
        val bloggerEnabled = findViewById<SwitchMaterial>(R.id.blogger_enabled)
        val bloggerClientId = findViewById<EditText>(R.id.blogger_client_id)
        val bloggerSecret = findViewById<EditText>(R.id.blogger_secret)
        val blogId = findViewById<EditText>(R.id.blog_id)
        
        // WordPress
        val wordpressEnabled = findViewById<SwitchMaterial>(R.id.wordpress_enabled)
        val wpSiteUrl = findViewById<EditText>(R.id.wp_site_url)
        val wpUsername = findViewById<EditText>(R.id.wp_username)
        val wpAppPassword = findViewById<EditText>(R.id.wp_app_password)

        // Pinterest
        val pinterestEnabled = findViewById<SwitchMaterial>(R.id.pinterest_enabled)
        val pinterestAppId = findViewById<EditText>(R.id.pinterest_app_id)
        val pinterestSecret = findViewById<EditText>(R.id.pinterest_secret)
        val pinterestBoardId = findViewById<EditText>(R.id.pinterest_board_id)

        // Gemini
        val geminiKeys = findViewById<EditText>(R.id.gemini_keys)

        // Load values
        bloggerEnabled.isChecked = prefs.getBoolean("blogger_enabled", true)
        bloggerClientId.setText(prefs.getString("blogger_client_id", ""))
        bloggerSecret.setText(prefs.getString("blogger_secret", ""))
        blogId.setText(prefs.getString("blog_id", ""))

        wordpressEnabled.isChecked = prefs.getBoolean("wordpress_enabled", false)
        wpSiteUrl.setText(prefs.getString("wp_site_url", ""))
        wpUsername.setText(prefs.getString("wp_username", ""))
        wpAppPassword.setText(prefs.getString("wp_app_password", ""))

        pinterestEnabled.isChecked = prefs.getBoolean("pinterest_enabled", true)
        pinterestAppId.setText(prefs.getString("pinterest_app_id", ""))
        pinterestSecret.setText(prefs.getString("pinterest_secret", ""))
        pinterestBoardId.setText(prefs.getString("pinterest_board_id", ""))

        geminiKeys.setText(geminiKeyManager.getAllKeys().joinToString("\n"))

        val saveButton = findViewById<Button>(R.id.save_settings)

        saveButton.setOnClickListener {
            saveButton.isEnabled = false
            
            lifecycleScope.launch {
                val editor = prefs.edit()
                
                // Save Blogger
                editor.putBoolean("blogger_enabled", bloggerEnabled.isChecked)
                editor.putString("blogger_client_id", bloggerClientId.text.toString())
                editor.putString("blogger_secret", bloggerSecret.text.toString())
                editor.putString("blog_id", blogId.text.toString())

                // Save WordPress
                editor.putBoolean("wordpress_enabled", wordpressEnabled.isChecked)
                editor.putString("wp_site_url", wpSiteUrl.text.toString())
                editor.putString("wp_username", wpUsername.text.toString())
                editor.putString("wp_app_password", wpAppPassword.text.toString())

                // Save Pinterest
                editor.putBoolean("pinterest_enabled", pinterestEnabled.isChecked)
                editor.putString("pinterest_app_id", pinterestAppId.text.toString())
                editor.putString("pinterest_secret", pinterestSecret.text.toString())
                editor.putString("pinterest_board_id", pinterestBoardId.text.toString())

                // Save Gemini Keys
                val keys = geminiKeys.text.toString().split("\n").filter { it.isNotBlank() }
                if (keys.isNotEmpty()) {
                    geminiKeyManager.updateKeys(keys)
                }

                editor.apply()
                saveButton.isEnabled = true
                Toast.makeText(this@SettingsActivity, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
