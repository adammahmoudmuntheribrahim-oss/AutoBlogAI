package com.example.articleautomator

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
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
        
        val bloggerClientId = findViewById<EditText>(R.id.blogger_client_id)
        val bloggerSecret = findViewById<EditText>(R.id.blogger_secret)
        val blogId = findViewById<EditText>(R.id.blog_id)
        val pinterestAppId = findViewById<EditText>(R.id.pinterest_app_id)
        val pinterestSecret = findViewById<EditText>(R.id.pinterest_secret)
        val pinterestBoardId = findViewById<EditText>(R.id.pinterest_board_id)
        val pinterestEnabled = findViewById<SwitchMaterial>(R.id.pinterest_enabled)
        val geminiKeys = findViewById<EditText>(R.id.gemini_keys)
        val lengthSpinner = findViewById<Spinner>(R.id.length_spinner)
        val personalitySpinner = findViewById<Spinner>(R.id.personality_spinner)
        val randomSwitch = findViewById<SwitchMaterial>(R.id.random_scheduling)
        val maxDelay = findViewById<EditText>(R.id.max_delay_minutes)

        // Load values
        bloggerClientId.setText(prefs.getString("blogger_client_id", ""))
        bloggerSecret.setText(prefs.getString("blogger_secret", ""))
        blogId.setText(prefs.getString("blog_id", ""))
        pinterestAppId.setText(prefs.getString("pinterest_app_id", ""))
        pinterestSecret.setText(prefs.getString("pinterest_secret", ""))
        pinterestBoardId.setText(prefs.getString("board_id", ""))
        pinterestEnabled.isChecked = prefs.getBoolean("pinterest_enabled", true)
        geminiKeys.setText(geminiKeyManager.getAllKeys().joinToString("\n"))
        randomSwitch.isChecked = prefs.getBoolean("random_scheduling", false)
        maxDelay.setText(prefs.getInt("max_delay_minutes", 240).toString())

        val saveButton = findViewById<Button>(R.id.save_settings)
        val progressBar = findViewById<ProgressBar>(R.id.settings_progress)

        saveButton.setOnClickListener {
            val keys = geminiKeys.text.toString().split("\n").filter { it.isNotBlank() }
            
            saveButton.isEnabled = false
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                val validKeys = mutableListOf<String>()
                val invalidKeys = mutableListOf<String>()
                
                for (key in keys) {
                    if (geminiKeyManager.validateKey(key)) {
                        validKeys.add(key)
                    } else {
                        invalidKeys.add(key)
                    }
                }
                
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true

                if (invalidKeys.isNotEmpty()) {
                    Toast.makeText(this@SettingsActivity, "بعض المفاتيح غير صالحة ولن يتم حفظها", Toast.LENGTH_LONG).show()
                }

                geminiKeyManager.updateKeys(validKeys)
                geminiKeys.setText(validKeys.joinToString("\n"))
                
                prefs.edit().apply {
                putString("blogger_client_id", bloggerClientId.text.toString())
                putString("blogger_secret", bloggerSecret.text.toString())
                putString("blog_id", blogId.text.toString())
                putString("pinterest_app_id", pinterestAppId.text.toString())
                putString("pinterest_secret", pinterestSecret.text.toString())
                putString("board_id", pinterestBoardId.text.toString())
                putBoolean("pinterest_enabled", pinterestEnabled.isChecked)
                putString("length_option", lengthSpinner.selectedItem.toString())
                putString("personality", WriterPersonality.values()[personalitySpinner.selectedItemPosition].name)
                putBoolean("random_scheduling", randomSwitch.isChecked)
                putInt("max_delay_minutes", maxDelay.text.toString().toIntOrNull() ?: 240)
                apply()
            }
            Toast.makeText(this@SettingsActivity, "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
            finish()
            }
        }
    }
}
