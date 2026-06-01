package com.example.articleautomator

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
        
        val bloggerClientId = findViewById<EditText>(R.id.blogger_client_id)
        val bloggerSecret = findViewById<EditText>(R.id.blogger_secret)
        val blogId = findViewById<EditText>(R.id.blog_id)
        val geminiKeys = findViewById<EditText>(R.id.gemini_keys)
        val lengthSpinner = findViewById<Spinner>(R.id.length_spinner)
        val personalitySpinner = findViewById<Spinner>(R.id.personality_spinner)
        val randomSwitch = findViewById<Switch>(R.id.random_scheduling)
        val maxDelay = findViewById<EditText>(R.id.max_delay_minutes)

        // Load values
        bloggerClientId.setText(prefs.getString("blogger_client_id", ""))
        bloggerSecret.setText(prefs.getString("blogger_secret", ""))
        blogId.setText(prefs.getString("blog_id", ""))
        geminiKeys.setText(geminiKeyManager.getAllKeys().joinToString("\n"))
        randomSwitch.isChecked = prefs.getBoolean("random_scheduling", false)
        maxDelay.setText(prefs.getInt("max_delay_minutes", 240).toString())

        findViewById<Button>(R.id.save_settings).setOnClickListener {
            val keys = geminiKeys.text.toString().split("\n").filter { it.isNotBlank() }
            geminiKeyManager.updateKeys(keys)
            
            prefs.edit().apply {
                putString("blogger_client_id", bloggerClientId.text.toString())
                putString("blogger_secret", bloggerSecret.text.toString())
                putString("blog_id", blogId.text.toString())
                putString("length_option", lengthSpinner.selectedItem.toString())
                putString("personality", WriterPersonality.values()[personalitySpinner.selectedItemPosition].name)
                putBoolean("random_scheduling", randomSwitch.isChecked)
                putInt("max_delay_minutes", maxDelay.text.toString().toIntOrNull() ?: 240)
                apply()
            }
            Toast.makeText(this, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
