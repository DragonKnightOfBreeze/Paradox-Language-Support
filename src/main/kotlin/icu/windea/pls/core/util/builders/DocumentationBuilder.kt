package icu.windea.pls.core.util.builders

import com.intellij.lang.documentation.DocumentationMarkup
import java.util.*

@Suppress("NOTHING_TO_INLINE")
inline fun buildDocumentation(): DocumentationBuilder {
    return DocumentationBuilderImpl()
}

inline fun buildDocumentation(block: DocumentationBuilder.() -> Unit): String {
    val builder = DocumentationBuilderImpl()
    builder.block()
    return builder.toString()
}

interface DocumentationBuilder {
    val content: StringBuilder

    fun append(string: String): DocumentationBuilder

    fun append(value: Any?): DocumentationBuilder

    override fun toString(): String

    fun appendBr(): DocumentationBuilder

    fun appendIndent(): DocumentationBuilder

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

class DocumentationBuilderImpl : DocumentationBuilder {
    private var sectionGroup: SortedMap<Int, MutableMap<String, String>>? = null

    override val content: StringBuilder = StringBuilder()

    override fun append(string: String) = apply { content.append(string) }

    override fun append(value: Any?) = apply { content.append(value) }

    override fun toString() = content.toString()

    override fun appendBr() = append("<br>")

    override fun appendIndent() = append("&nbsp;&nbsp;&nbsp;&nbsp;")

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
