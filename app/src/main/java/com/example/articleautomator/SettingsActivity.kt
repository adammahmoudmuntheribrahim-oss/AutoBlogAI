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
        
        // Input Fields
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

        // Layouts for error/helper messages
        val layoutBloggerClientId = findViewById<TextInputLayout>(R.id.layout_blogger_client_id)
        val layoutBloggerSecret = findViewById<TextInputLayout>(R.id.layout_blogger_secret)
        val layoutBlogId = findViewById<TextInputLayout>(R.id.layout_blog_id)
        val layoutPinterestAppId = findViewById<TextInputLayout>(R.id.layout_pinterest_app_id)
        val layoutPinterestSecret = findViewById<TextInputLayout>(R.id.layout_pinterest_secret)
        val layoutPinterestBoardId = findViewById<TextInputLayout>(R.id.layout_pinterest_board_id)
        val layoutGeminiKeys = findViewById<TextInputLayout>(R.id.layout_gemini_keys)
        val layoutMaxDelay = findViewById<TextInputLayout>(R.id.layout_max_delay)

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

        // Set spinner selections
        val lengthOptions = resources.getStringArray(R.array.length_options)
        val savedLength = prefs.getString("length_option", "متوسط")
        val lengthIndex = lengthOptions.indexOf(savedLength).coerceAtLeast(0)
        lengthSpinner.setSelection(lengthIndex)

        val savedPersonalityName = prefs.getString("personality", WriterPersonality.REALIST.name)
        try {
            val savedPersonality = WriterPersonality.valueOf(savedPersonalityName!!)
            personalitySpinner.setSelection(savedPersonality.ordinal)
        } catch (e: Exception) {
            personalitySpinner.setSelection(WriterPersonality.REALIST.ordinal)
        }

        val saveButton = findViewById<Button>(R.id.save_settings)
        val progressBar = findViewById<ProgressBar>(R.id.settings_progress)

        saveButton.setOnClickListener {
            saveButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            
            // Clear previous errors and helpers
            val layouts = listOf(
                layoutBloggerClientId, layoutBloggerSecret, layoutBlogId,
                layoutPinterestAppId, layoutPinterestSecret, layoutPinterestBoardId,
                layoutGeminiKeys, layoutMaxDelay
            )
            layouts.forEach { 
                it.error = null
                it.helperText = null
            }

            lifecycleScope.launch {
                val editor = prefs.edit()
                var hasAnyError = false

                // 1. Validate Blogger Settings
                if (bloggerClientId.text.isBlank()) {
                    layoutBloggerClientId.error = "هذا الحقل مطلوب"
                    hasAnyError = true
                } else {
                    editor.putString("blogger_client_id", bloggerClientId.text.toString())
                    layoutBloggerClientId.helperText = "تم الحفظ بنجاح"
                }

                if (bloggerSecret.text.isBlank()) {
                    layoutBloggerSecret.error = "هذا الحقل مطلوب"
                    hasAnyError = true
                } else {
                    editor.putString("blogger_secret", bloggerSecret.text.toString())
                    layoutBloggerSecret.helperText = "تم الحفظ بنجاح"
                }

                if (blogId.text.isBlank()) {
                    layoutBlogId.error = "هذا الحقل مطلوب"
                    hasAnyError = true
                } else {
                    editor.putString("blog_id", blogId.text.toString())
                    layoutBlogId.helperText = "تم الحفظ بنجاح"
                }

                // 2. Validate Pinterest Settings (Only if enabled)
                if (pinterestEnabled.isChecked) {
                    if (pinterestAppId.text.isBlank()) {
                        layoutPinterestAppId.error = "هذا الحقل مطلوب عند تفعيل Pinterest"
                        hasAnyError = true
                    } else {
                        editor.putString("pinterest_app_id", pinterestAppId.text.toString())
                        layoutPinterestAppId.helperText = "تم الحفظ بنجاح"
                    }

                    if (pinterestSecret.text.isBlank()) {
                        layoutPinterestSecret.error = "هذا الحقل مطلوب عند تفعيل Pinterest"
                        hasAnyError = true
                    } else {
                        editor.putString("pinterest_secret", pinterestSecret.text.toString())
                        layoutPinterestSecret.helperText = "تم الحفظ بنجاح"
                    }

                    if (pinterestBoardId.text.isBlank()) {
                        layoutPinterestBoardId.error = "هذا الحقل مطلوب عند تفعيل Pinterest"
                        hasAnyError = true
                    } else {
                        editor.putString("board_id", pinterestBoardId.text.toString())
                        layoutPinterestBoardId.helperText = "تم الحفظ بنجاح"
                    }
                } else {
                    // Still save values if they exist, but don't require them
                    editor.putString("pinterest_app_id", pinterestAppId.text.toString())
                    editor.putString("pinterest_secret", pinterestSecret.text.toString())
                    editor.putString("board_id", pinterestBoardId.text.toString())
                }
                editor.putBoolean("pinterest_enabled", pinterestEnabled.isChecked)

                // 3. Validate Gemini Keys (Independent validation for each key)
                val keys = geminiKeys.text.toString().split("\n").filter { it.isNotBlank() }
                if (keys.isEmpty()) {
                    layoutGeminiKeys.error = "يجب إضافة مفتاح Gemini واحد على الأقل"
                    hasAnyError = true
                } else {
                    val validKeys = mutableListOf<String>()
                    val invalidKeys = mutableListOf<String>()
                    
                    for (key in keys) {
                        if (geminiKeyManager.validateKey(key)) {
                            validKeys.add(key)
                        } else {
                            invalidKeys.add(key)
                        }
                    }
                    
                    geminiKeyManager.updateKeys(validKeys)
                    
                    if (invalidKeys.isNotEmpty()) {
                        layoutGeminiKeys.error = "تم رفض المفاتيح الخاطئة:\n${invalidKeys.joinToString("\n")}"
                        geminiKeys.setText((validKeys + invalidKeys).joinToString("\n"))
                        hasAnyError = true
                    } else {
                        layoutGeminiKeys.helperText = "تم التحقق وحفظ جميع المفاتيح بنجاح"
                        geminiKeys.setText(validKeys.joinToString("\n"))
                    }
                }

                // 4. Save Content Options
                editor.putString("length_option", lengthSpinner.selectedItem.toString())
                editor.putString("personality", WriterPersonality.values()[personalitySpinner.selectedItemPosition].name)
                editor.putBoolean("random_scheduling", randomSwitch.isChecked)
                
                val delayVal = maxDelay.text.toString().toIntOrNull()
                if (delayVal == null || delayVal <= 0) {
                    layoutMaxDelay.error = "أدخل قيمة صحيحة أكبر من صفر"
                    hasAnyError = true
                } else {
                    editor.putInt("max_delay_minutes", delayVal)
                    layoutMaxDelay.helperText = "تم الحفظ بنجاح"
                }

                // Final Save
                editor.apply()
                
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true

                if (hasAnyError) {
                    Toast.makeText(this@SettingsActivity, "تم حفظ الحقول الصحيحة، يرجى مراجعة الحقول الخاطئة", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "تم حفظ جميع الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
