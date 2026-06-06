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
        val previewEnabled = findViewById<SwitchMaterial>(R.id.preview_enabled)
        val geminiKeys = findViewById<EditText>(R.id.gemini_keys)
        val lengthSpinner = findViewById<Spinner>(R.id.length_spinner)
        val personalitySpinner = findViewById<Spinner>(R.id.personality_spinner)
        val languageSpinner = findViewById<Spinner>(R.id.language_spinner)
        val imageSourceSpinner = findViewById<Spinner>(R.id.image_source_spinner)
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
        previewEnabled.isChecked = prefs.getBoolean("preview_enabled", false)
        geminiKeys.setText(geminiKeyManager.getAllKeys().joinToString("\n"))
        randomSwitch.isChecked = prefs.getBoolean("random_scheduling", false)
        maxDelay.setText(prefs.getInt("max_delay_minutes", 240).toString())

        // Set spinner selections
        val lengthOptions = resources.getStringArray(R.array.length_options)
        val savedLength = prefs.getString("length_option", "متوسط")
        lengthSpinner.setSelection(lengthOptions.indexOf(savedLength).coerceAtLeast(0))

        val languageOptions = resources.getStringArray(R.array.language_options)
        val savedLang = prefs.getString("language", "الإنجليزية")
        languageSpinner.setSelection(languageOptions.indexOf(savedLang).coerceAtLeast(0))

        val imageSourceOptions = resources.getStringArray(R.array.image_source_options)
        val savedImgSrc = prefs.getString("image_source", "Gemini Image (مجاني)")
        imageSourceSpinner.setSelection(imageSourceOptions.indexOf(savedImgSrc).coerceAtLeast(0))

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
            
            val layouts = listOf(
                layoutBloggerClientId, layoutBloggerSecret, layoutBlogId,
                layoutPinterestAppId, layoutPinterestSecret, layoutPinterestBoardId,
                layoutGeminiKeys, layoutMaxDelay
            )
            layouts.forEach { it.error = null; it.helperText = null }

            lifecycleScope.launch {
                val editor = prefs.edit()
                var hasAnyError = false

                // Validate Blogger
                if (bloggerClientId.text.isBlank()) { layoutBloggerClientId.error = "مطلوب"; hasAnyError = true }
                else { editor.putString("blogger_client_id", bloggerClientId.text.toString()); layoutBloggerClientId.helperText = "تم" }

                if (bloggerSecret.text.isBlank()) { layoutBloggerSecret.error = "مطلوب"; hasAnyError = true }
                else { editor.putString("blogger_secret", bloggerSecret.text.toString()); layoutBloggerSecret.helperText = "تم" }

                if (blogId.text.isBlank()) { layoutBlogId.error = "مطلوب"; hasAnyError = true }
                else { editor.putString("blog_id", blogId.text.toString()); layoutBlogId.helperText = "تم" }

                // Validate Pinterest
                if (pinterestEnabled.isChecked) {
                    if (pinterestAppId.text.isBlank()) { layoutPinterestAppId.error = "مطلوب"; hasAnyError = true }
                    else { editor.putString("pinterest_app_id", pinterestAppId.text.toString()); layoutPinterestAppId.helperText = "تم" }
                }
                editor.putBoolean("pinterest_enabled", pinterestEnabled.isChecked)
                editor.putBoolean("preview_enabled", previewEnabled.isChecked)

                // Validate Gemini
                val keys = geminiKeys.text.toString().split("\n").filter { it.isNotBlank() }
                if (keys.isEmpty()) { layoutGeminiKeys.error = "مطلوب مفتاح واحد على الأقل"; hasAnyError = true }
                else {
                    val validKeys = keys.filter { geminiKeyManager.validateKey(it) }
                    geminiKeyManager.updateKeys(validKeys)
                    if (validKeys.size < keys.size) { layoutGeminiKeys.error = "بعض المفاتيح خاطئة"; hasAnyError = true }
                    else layoutGeminiKeys.helperText = "تم التحقق"
                }

                // Save Spinners
                editor.putString("length_option", lengthSpinner.selectedItem.toString())
                editor.putString("language", languageSpinner.selectedItem.toString())
                editor.putString("image_source", imageSourceSpinner.selectedItem.toString())
                editor.putString("personality", WriterPersonality.values()[personalitySpinner.selectedItemPosition].name)
                
                editor.putBoolean("random_scheduling", randomSwitch.isChecked)
                val delayVal = maxDelay.text.toString().toIntOrNull()
                if (delayVal == null || delayVal <= 0) { layoutMaxDelay.error = "خطأ"; hasAnyError = true }
                else { editor.putInt("max_delay_minutes", delayVal); layoutMaxDelay.helperText = "تم" }

                editor.apply()
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true

                if (!hasAnyError) {
                    Toast.makeText(this@SettingsActivity, "تم الحفظ", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@SettingsActivity, "يرجى مراجعة الأخطاء", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
