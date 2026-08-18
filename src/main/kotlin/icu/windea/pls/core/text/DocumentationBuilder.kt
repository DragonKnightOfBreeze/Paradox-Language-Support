@file:Suppress("unused")

package icu.windea.pls.core.text

import com.intellij.lang.documentation.DocumentationMarkup
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.toFileUrl
import java.util.*

fun buildDocumentation(): DocumentationBuilder {
    return DocumentationBuilderImpl()
}

inline fun buildDocumentation(block: DocumentationBuilder.() -> Unit): String {
    val builder = buildDocumentation()
    builder.block()
    return builder.toString()
}

interface DocumentationBuilder : Appendable {
    val content: StringBuilder

    fun append(string: String): DocumentationBuilder

    fun append(value: Any?): DocumentationBuilder

    override fun toString(): String

    fun indent(): DocumentationBuilder

    fun br(): DocumentationBuilder

    fun link(refText: String, label: String, escapeLabel: Boolean = true): DocumentationBuilder

    fun image(url: String, local: Boolean = true): DocumentationBuilder

    fun image(url: String, width: Int, height: Int, local: Boolean = true): DocumentationBuilder

    fun definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun sections(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun section(title: CharSequence, value: CharSequence): DocumentationBuilder

    fun grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun initSections()

    fun getSections(index: Int): MutableMap<String, String>?

    fun buildSections()
}

// region Implementations

private class DocumentationBuilderImpl(
    override val content: StringBuilder = StringBuilder()
) : DocumentationBuilder, Appendable by content {
    private var sectionGroup: SortedMap<Int, MutableMap<String, String>>? = null

    override fun append(string: String) = apply { content.append(string) }

    override fun append(value: Any?) = apply { content.append(value) }

    override fun toString() = content.toString()

    override fun indent() = append("&nbsp;&nbsp;&nbsp;&nbsp;")

    override fun br() = append("<br/>")

    override fun link(refText: String, label: String, escapeLabel: Boolean): DocumentationBuilder {
        append("<a href=\"").append(refText).append("\">")
        if (escapeLabel) append(label.escapeXml()) else append(label)
        append("</a>")
        return this
    }

    override fun image(url: String, local: Boolean): DocumentationBuilder {
        val finalUrl = if (local) url.toFileUrl() else url
        append("<img src=\"").append(finalUrl).append("\"/>")
        return this
    }

    override fun image(url: String, width: Int, height: Int, local: Boolean): DocumentationBuilder {
        // NOTE 这里存在限制，不能使用 `style="..."`
        val finalUrl = if (local) url.toFileUrl() else url
        append("<img src=\"").append(finalUrl).append("\"")
        append(" width=\"").append(width).append("\" height=\"").append(height).append("\" vspace=\"0\" hspace=\"0\"")
        append("/>")
        return this
    }

    override fun definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
        append(DocumentationMarkup.DEFINITION_START)
        block(this)
        append(DocumentationMarkup.DEFINITION_END)
        return this
    }

    override fun content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
        append(DocumentationMarkup.CONTENT_START)
        block(this)
        append(DocumentationMarkup.CONTENT_END)
        return this
    }

    override fun sections(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
        append(DocumentationMarkup.SECTIONS_START)
        block(this)
        append(DocumentationMarkup.SECTIONS_END)
        return this
    }

    override fun section(title: CharSequence, value: CharSequence): DocumentationBuilder {
        append(DocumentationMarkup.SECTION_HEADER_START)
        append(title).append(": ")
        append(DocumentationMarkup.SECTION_SEPARATOR).append("<p>")
        append(value)
        append(DocumentationMarkup.SECTION_END)
        return this
    }

    override fun grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
        append(DocumentationMarkup.GRAYED_START)
        block(this)
        append(DocumentationMarkup.GRAYED_END)
        return this
    }

    override fun initSections() {
        sectionGroup = sortedMapOf()
    }

    override fun getSections(index: Int): MutableMap<String, String>? {
        return sectionGroup?.getOrPut(index) { mutableMapOf() }
    }

    override fun buildSections() {
        val sectionGroup = sectionGroup
        if (sectionGroup.isNullOrEmpty()) return
        sections {
            for (sections in sectionGroup.values) {
                for ((key, value) in sections) {
                    section(key, value)
                }
            }
        }
    }
}

// endregion
