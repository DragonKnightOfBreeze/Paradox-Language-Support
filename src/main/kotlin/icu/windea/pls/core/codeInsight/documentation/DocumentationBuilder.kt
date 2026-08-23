@file:Suppress("unused")

package icu.windea.pls.core.codeInsight.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.platform.backend.documentation.DocumentationResult
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.toFileUrl
import java.awt.Image
import java.util.*

fun buildDocumentation(hint: Boolean = true): DocumentationBuilder {
    return DocumentationBuilderImpl(hint)
}

inline fun buildDocumentation(hint: Boolean = true, block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    return buildDocumentation(hint).apply(block)
}

interface DocumentationBuilder : Appendable {
    val hint: Boolean
    val content: StringBuilder

    fun isEmpty(): Boolean

    fun append(string: String): DocumentationBuilder

    fun append(value: Any?): DocumentationBuilder

    override fun toString(): String

    fun toDocumentation(): DocumentationResult.Documentation

    fun indent(): DocumentationBuilder

    fun br(): DocumentationBuilder

    fun link(refText: String, label: String, escapeLabel: Boolean = true): DocumentationBuilder

    fun image(url: String, local: Boolean = true): DocumentationBuilder

    fun image(url: String, width: Int, height: Int, local: Boolean = true): DocumentationBuilder

    fun definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun sections(block: DocumentationBuilder.() -> Unit): DocumentationBuilder

    fun section(title: CharSequence, value: CharSequence): DocumentationBuilder

    fun getSections(index: Int): MutableMap<String, String>

    fun buildSections()

    fun registerImages(images: Map<String, Image>): DocumentationBuilder

    fun registerExternalUrl(externalUrl: String?): DocumentationBuilder
}

// region Implementations

private class DocumentationBuilderImpl(
    override val hint: Boolean,
    override val content: StringBuilder = StringBuilder()
) : DocumentationBuilder, Appendable by content {
    private val sectionGroups: SortedMap<Int, MutableMap<String, String>> = sortedMapOf()
    private var images: Map<String, Image> = emptyMap()
    private var externalUrl: String? = null

    override fun isEmpty() = content.isEmpty()

    override fun append(string: String) = apply { content.append(string) }

    override fun append(value: Any?) = apply { content.append(value) }

    override fun toString() = content.toString()

    override fun toDocumentation(): DocumentationResult.Documentation {
        return DocumentationResult.documentation(content.toString()).images(images).externalUrl(externalUrl)
    }

    override fun indent(): DocumentationBuilderImpl {
        return append("&nbsp;&nbsp;&nbsp;&nbsp;")
    }

    override fun br(): DocumentationBuilderImpl {
        return append("<br/>")
    }

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
        // 注意：这里存在限制，不能使用 `style="..."`
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

    override fun grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
        append(DocumentationMarkup.GRAYED_START)
        block(this)
        append(DocumentationMarkup.GRAYED_END)
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

    override fun getSections(index: Int): MutableMap<String, String> {
        return sectionGroups.getOrPut(index) { mutableMapOf() }
    }

    override fun buildSections() {
        val sectionGroup = sectionGroups
        if (sectionGroup.isEmpty()) return
        sections {
            for (sections in sectionGroup.values) {
                for ((key, value) in sections) {
                    section(key, value)
                }
            }
        }
    }

    override fun registerImages(images: Map<String, Image>): DocumentationBuilder {
        this.images = images
        return this
    }

    override fun registerExternalUrl(externalUrl: String?): DocumentationBuilder {
        this.externalUrl = externalUrl
        return this
    }
}

// endregion
