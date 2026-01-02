@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.codeInsight.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import java.util.*

/** 使用 [DocumentationBuilder] 构建文档片段并返回最终字符串。*/
inline fun buildDocumentation(builderAction: DocumentationBuilder.() -> Unit): String {
    val builder = DocumentationBuilder()
    builder.builderAction()
    return builder.content.toString()
}

/** 追加“定义区域”。*/
inline fun DocumentationBuilder.definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.DEFINITION_START)
    block(this)
    append(DocumentationMarkup.DEFINITION_END)
    return this
}

/** 追加“内容区域”。*/
inline fun DocumentationBuilder.content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.CONTENT_START)
    block(this)
    append(DocumentationMarkup.CONTENT_END)
    return this
}

/** 追加“分节容器”。*/
inline fun DocumentationBuilder.sections(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.SECTIONS_START)
    block(this)
    append(DocumentationMarkup.SECTIONS_END)
    return this
}

/** 追加一节，标题 [title] 与内容 [value]。*/
inline fun DocumentationBuilder.section(title: CharSequence, value: CharSequence): DocumentationBuilder {
    append(DocumentationMarkup.SECTION_HEADER_START)
    append(title).append(": ")
    append(DocumentationMarkup.SECTION_SEPARATOR).append("<p>")
    append(value)
    append(DocumentationMarkup.SECTION_END)
    return this
}

/** 在“灰色文字”样式中渲染 [block] 内容。*/
inline fun DocumentationBuilder.grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.GRAYED_START)
    block(this)
    append(DocumentationMarkup.GRAYED_END)
    return this
}

var DocumentationBuilder.sectionGroup: SortedMap<Int, MutableMap<String, String>>? by registerKey(DocumentationBuilder.Keys)

/** 初始化分节组 [sectionGroup]。*/
fun DocumentationBuilder.initSections() {
    sectionGroup = sortedMapOf()
}

/** 得到分节组 [sectionGroup] 中的指定索引 [index] 的分节映射。如果未初始化则返回 `null`。*/
fun DocumentationBuilder.getSections(index: Int): MutableMap<String, String>? {
    return sectionGroup?.getOrPut(index) { mutableMapOf() }
}

/** 根据分节组 [sectionGroup] 批量构建分节输出。如果未初始化或为空则不输出任何内容。*/
fun DocumentationBuilder.buildSections() {
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
