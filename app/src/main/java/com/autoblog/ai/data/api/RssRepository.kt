package com.autoblog.ai.data.api

class RssRepository {

    suspend fun fetchArticles(): List<String> {
        // TODO: استبدال بجلب حقيقي من خلاصات RSS
        return listOf(
            "مقال عن الذكاء الاصطناعي",
            "تطبيقات أندرويد الجديدة",
            "أخبار العملات الرقمية"
        )
    }
}
