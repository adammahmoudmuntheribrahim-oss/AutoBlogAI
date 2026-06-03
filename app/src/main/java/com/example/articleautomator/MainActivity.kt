package com.example.articleautomator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.articleautomator.data.LogRepository
import com.example.articleautomator.ui.LogAdapter
import com.example.articleautomator.ui.RssAdapter
import com.example.articleautomator.workflow.BloggerPublisher
import com.example.articleautomator.workflow.PinterestPublisher
import com.example.articleautomator.worker.ArticleWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    
    @Inject lateinit var logRepository: LogRepository
    @Inject lateinit var bloggerPublisher: BloggerPublisher
    @Inject lateinit var pinterestPublisher: PinterestPublisher

    private lateinit var logAdapter: LogAdapter
    private lateinit var rssAdapter: RssAdapter
    private val rssUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        setupRecyclerViews()
        setupButtons()
        observeLogs()
    }

    override fun onResume() {
        super.onResume()
        updatePinterestButtonVisibility()
    }

    private fun updatePinterestButtonVisibility() {
        val pinterestEnabled = getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("pinterest_enabled", true)
        findViewById<Button>(R.id.auth_pinterest)?.visibility = if (pinterestEnabled) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupRecyclerViews() {
        val logRecycler = findViewById<RecyclerView>(R.id.log_recycler)
        logAdapter = LogAdapter(mutableListOf())
        logRecycler.adapter = logAdapter
        logRecycler.layoutManager = LinearLayoutManager(this)

        val rssRecycler = findViewById<RecyclerView>(R.id.rss_recycler)
        val savedUrls = getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getStringSet("rss_urls", emptySet())?.toList() ?: emptyList()
        rssUrls.addAll(savedUrls)
        rssAdapter = RssAdapter(rssUrls) { position ->
            rssUrls.removeAt(position)
            rssAdapter.notifyItemRemoved(position)
            saveRssUrls()
        }
        rssRecycler.adapter = rssAdapter
        rssRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        val addRssButton = findViewById<Button>(R.id.add_rss_button)
        val rssProgressBar = findViewById<ProgressBar>(R.id.rss_progress)
        val rssInput = findViewById<EditText>(R.id.new_rss_url)

        addRssButton.setOnClickListener {
            val url = rssInput.text.toString().trim()
            if (url.isNotEmpty() && url.startsWith("http")) {
                addRssButton.isEnabled = false
                rssProgressBar.visibility = android.view.View.VISIBLE
                
                lifecycleScope.launch {
                    val isValid = viewModel.validateRssUrl(url)
                    rssProgressBar.visibility = android.view.View.GONE
                    addRssButton.isEnabled = true
                    
                    if (isValid) {
                        rssAdapter.addUrl(url)
                        rssInput.text.clear()
                        saveRssUrls()
                        Toast.makeText(this@MainActivity, "تمت إضافة الرابط بنجاح", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "رابط RSS غير صالح أو لا يحتوي على مقالات", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val authLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val code = result.data?.getStringExtra("code") ?: return@registerForActivityResult
                val provider = result.data?.getStringExtra("provider") ?: "google"
                
                lifecycleScope.launch {
                    try {
                        if (provider == "google") {
                            bloggerPublisher.authenticateWithCode(code)
                            logRepository.addLog("تم ربط Blogger بنجاح", "SUCCESS")
                        } else {
                            pinterestPublisher.authenticateWithCode(code)
                            logRepository.addLog("تم ربط Pinterest بنجاح", "SUCCESS")
                        }
                    } catch (e: Exception) {
                        logRepository.addLog("فشل الربط: ${e.message}", "ERROR")
                    }
                }
            }
        }

        findViewById<Button>(R.id.auth_blogger).setOnClickListener {
            try {
                val intent = Intent(this, AuthActivity::class.java).apply {
                    putExtra("auth_url", bloggerPublisher.getAuthUrl())
                }
                authLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.auth_pinterest)?.setOnClickListener {
            try {
                val intent = Intent(this, AuthActivity::class.java).apply {
                    putExtra("auth_url", pinterestPublisher.getAuthUrl())
                }
                authLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.start_worker).setOnClickListener {
            val isRandom = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("random_scheduling", false)
            if (isRandom) {
                val request = OneTimeWorkRequestBuilder<ArticleWorker>().build()
                WorkManager.getInstance(this).enqueueUniqueWork("ArticleWorker_Random", ExistingWorkPolicy.REPLACE, request)
            } else {
                val request = PeriodicWorkRequestBuilder<ArticleWorker>(1, TimeUnit.HOURS).build()
                WorkManager.getInstance(this).enqueueUniquePeriodicWork("ArticleWorker_Periodic", ExistingPeriodicWorkPolicy.UPDATE, request)
            }
            Toast.makeText(this, "بدأت الجدولة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeLogs() {
        viewModel.logs.observe(this) { logs ->
            logAdapter.updateData(logs)
            findViewById<RecyclerView>(R.id.log_recycler).scrollToPosition(logs.size - 1)
        }
    }

    private fun saveRssUrls() {
        getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putStringSet("rss_urls", rssUrls.toSet())
            .apply()
    }
}
