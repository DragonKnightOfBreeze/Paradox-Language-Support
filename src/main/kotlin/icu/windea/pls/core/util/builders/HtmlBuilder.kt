@file:Suppress("unused")

package icu.windea.pls.core.util.builders

import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.toFileUrl

fun buildHtml(): HtmlBuilder {
    return HtmlBuilderImpl()
}

inline fun buildHtml(block: HtmlBuilder.() -> Unit): HtmlBuilder {
    return buildHtml().apply(block)
}

interface HtmlBuilder : Appendable {
    val content: StringBuilder

    fun isEmpty(): Boolean

    fun append(string: String): HtmlBuilder

    fun append(value: Any?): HtmlBuilder

    override fun toString(): String

    fun indent(): HtmlBuilder

    fun br(): HtmlBuilder

    fun link(refText: String, label: String, escapeLabel: Boolean = true): HtmlBuilder

    fun image(url: String, local: Boolean = true): HtmlBuilder

    fun image(url: String, width: Int, height: Int, local: Boolean = true): HtmlBuilder
}

// region Implementations

private class HtmlBuilderImpl(
    override val content: StringBuilder = StringBuilder()
) : HtmlBuilder, Appendable by content {
    override fun isEmpty() = content.isEmpty()

    override fun append(string: String) = apply { content.append(string) }

    override fun append(value: Any?) = apply { content.append(value) }

    override fun toString() = content.toString()

    override fun indent(): HtmlBuilderImpl {
        return append("&nbsp;&nbsp;&nbsp;&nbsp;")
    }

    override fun br(): HtmlBuilderImpl {
        return append("<br/>")
    }

    override fun link(refText: String, label: String, escapeLabel: Boolean): HtmlBuilder {
        val finalRefText = refText.escapeXml()
        content.append("<a href=\"").append(finalRefText).append("\">")
        val finalLabel = if (escapeLabel) label.escapeXml() else label
        append(finalLabel)
        append("</a>")
        return this
    }

    override fun image(url: String, local: Boolean): HtmlBuilder {
        val finalUrl = if (local) url.toFileUrl() else url
        append("<img src=\"").append(finalUrl).append("\"/>")
        return this
    }

    override fun image(url: String, width: Int, height: Int, local: Boolean): HtmlBuilder {
        val finalUrl = if (local) url.toFileUrl() else url
        append("<img src=\"").append(finalUrl).append("\"")
        append(" style=\"width:").append(width).append("px; height:").append(height).append("px;\"")
        append("/>")
        return this
    }
}

// endregion
