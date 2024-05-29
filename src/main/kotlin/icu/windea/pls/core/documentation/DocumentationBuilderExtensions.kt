@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.documentation

import com.intellij.lang.documentation.*
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

inline fun buildDocumentation(builderAction: DocumentationBuilder.() -> Unit): String {
    val builder = DocumentationBuilder()
    builder.builderAction()
    return builder.content.toString()
}

inline fun DocumentationBuilder.definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.DEFINITION_START)
    block(this)
    append(DocumentationMarkup.DEFINITION_END)
    return this
}

inline fun DocumentationBuilder.content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.CONTENT_START)
    block(this)
    append(DocumentationMarkup.CONTENT_END)
    return this
}

inline fun DocumentationBuilder.sections(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.SECTIONS_START)
    block(this)
    append(DocumentationMarkup.SECTIONS_END)
    return this
}

inline fun DocumentationBuilder.section(title: CharSequence, value: CharSequence): DocumentationBuilder {
    append(DocumentationMarkup.SECTION_HEADER_START)
    append(title).append(": ")
    append(DocumentationMarkup.SECTION_SEPARATOR).append("<p>")
    append(value)
    append(DocumentationMarkup.SECTION_END)
    return this
}

inline fun DocumentationBuilder.grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.GRAYED_START)
    block(this)
    append(DocumentationMarkup.GRAYED_END)
    return this
}

fun DocumentationBuilder.buildSections(sectionsList: List<Map<String, String>>): DocumentationBuilder {
    sections {
        for(sections in sectionsList) {
            for((key, value) in sections) {
                section(key, value)
            }
        }
    }
    return this
}