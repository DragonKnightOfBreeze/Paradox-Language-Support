package icu.windea.pls.core.util.builders

import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.toFileUrl

fun buildHtml(): HtmlBuilder {
    return HtmlBuilderImpl()
}

@Suppress("unused")
inline fun buildHtml(block: HtmlBuilder.() -> Unit): String {
    val builder = buildHtml()
    builder.block()
    return builder.toString()
}

interface HtmlBuilder {
    val content: StringBuilder

    fun append(string: String): HtmlBuilder

    fun append(value: Any?): HtmlBuilder

    override fun toString(): String

    fun appendLink(refText: String, label: String, escapeLabel: Boolean = true): HtmlBuilder

    fun appendImage(url: String, local: Boolean = true): HtmlBuilder

    fun appendImage(url: String, width: Int, height: Int, local: Boolean = true): HtmlBuilder
}

// region Implementations

private class HtmlBuilderImpl : HtmlBuilder {
    override val content: StringBuilder = StringBuilder()

    override fun append(string: String) = apply { content.append(string) }

    override fun append(value: Any?) = apply { content.append(value) }

    override fun toString() = content.toString()

    override fun appendLink(refText: String, label: String, escapeLabel: Boolean): HtmlBuilder {
        append("<a href=\"").append(refText.escapeXml()).append("\">")
        if (escapeLabel) append(label.escapeXml()) else append(label)
        append("</a>")
        return this
    }

    override fun appendImage(url: String, local: Boolean): HtmlBuilder {
        val finalUrl = if (local) url.toFileUrl() else url
        append("<img src=\"").append(finalUrl).append("\"/>")
        return this
    }

    override fun appendImage(url: String, width: Int, height: Int, local: Boolean): HtmlBuilder {
        val finalUrl = if (local) url.toFileUrl() else url
        append("<img src=\"").append(finalUrl).append("\"")
        append(" style=\"width:").append(width).append("px; height:").append(height).append("px;\"")
        append("/>")
        return this
    }
}

// endregion
