@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.documentation

import com.intellij.lang.documentation.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*

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
    append(DocumentationMarkup.SECTIONS_END)
    return this
}

inline fun DocumentationBuilder.grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.GRAYED_START)
    block(this)
    append(DocumentationMarkup.GRAYED_END)
    return this
}

var DocumentationBuilder.sectionsList: List<MutableMap<String, String>>? by createKeyDelegate(DocumentationBuilder.Keys)

fun DocumentationBuilder.initSections(listSize: Int) {
    sectionsList = List(listSize) { mutableMapOf() }
}

fun DocumentationBuilder.getSections(index: Int): MutableMap<String, String>? {
    return sectionsList?.getOrNull(index)
}

fun DocumentationBuilder.buildSections() {
    val sectionsList = this.sectionsList
    if(sectionsList.isNullOrEmpty()) return
    sections {
        for(sections in sectionsList) {
            for((key, value) in sections) {
                section(key, value)
            }
        }
    }
}