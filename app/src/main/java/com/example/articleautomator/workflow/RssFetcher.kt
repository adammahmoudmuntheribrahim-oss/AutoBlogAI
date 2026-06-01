package com.example.articleautomator.workflow

import com.example.articleautomator.model.RssItem
import com.rometools.rome.feed.synd.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssFetcher @Inject constructor() {
    fun fetchLatest(feedUrl: String): RssItem? {
        return try {
            val feed = SyndFeedInput().build(XmlReader(URL(feedUrl)))
            val entry = feed.entries.firstOrNull() ?: return null
            val description = entry.description?.value ?: ""
            RssItem(
                title = entry.title,
                link = entry.link,
                description = description,
                guid = entry.uri ?: entry.link,
                contentHash = md5(description)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
