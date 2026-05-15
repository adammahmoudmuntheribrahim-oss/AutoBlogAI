package com.autoblog.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.utils.LocalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Article>>(emptyList())
    val posts = _posts.asStateFlow()

    fun loadPosts(storage: LocalStorage) {
        viewModelScope.launch {
            _posts.value = storage.loadArticles()
        }
    }
}
