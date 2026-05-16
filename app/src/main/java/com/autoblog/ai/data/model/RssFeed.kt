package com.autoblog.ai.data.model

import kotlinx.serialization.Serializable
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
@Serializable
data class RssFeed(
    @field:Element(name = "channel")
    @Serializable
    var channel: Channel = Channel()
)

@Root(name = "channel", strict = false)
@Serializable
data class Channel(
    @field:Element(name = "title")
    @Serializable
    var title: String = "",

    @field:Element(name = "link")
    @Serializable
    var link: String = "",

    @field:ElementList(inline = true, name = "item")
    @Serializable
    var articles: List<ArticleItem> = listOf()
)

@Root(name = "item", strict = false)
@Serializable
data class ArticleItem(
    @field:Element(name = "title")
    @Serializable
    var title: String = "",

    @field:Element(name = "link")
    @Serializable
    var link: String = "",

    @field:Element(name = "description", required = false)
    @Serializable
    var description: String = ""
)
