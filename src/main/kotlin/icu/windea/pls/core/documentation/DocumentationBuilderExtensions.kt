@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

/** 使用 [DocumentationBuilder] 构建文档片段并返回最终字符串。*/
inline fun buildDocumentation(builderAction: DocumentationBuilder.() -> Unit): String {
    val builder = DocumentationBuilder()
    builder.builderAction()
    return builder.content.toString()
}

/** 追加“定义区域”，包裹在 [DocumentationMarkup.DEFINITION_START]/[DocumentationMarkup.DEFINITION_END]。*/
inline fun DocumentationBuilder.definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.DEFINITION_START)
    block(this)
    append(DocumentationMarkup.DEFINITION_END)
    return this
}

/** 追加“内容区域”，包裹在 [DocumentationMarkup.CONTENT_START]/[DocumentationMarkup.CONTENT_END]。*/
inline fun DocumentationBuilder.content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.CONTENT_START)
    block(this)
    append(DocumentationMarkup.CONTENT_END)
    return this
}

/** 追加“分节容器”，包裹在 [DocumentationMarkup.SECTIONS_START]/[DocumentationMarkup.SECTIONS_END]。*/
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

/** 存放“分节列表”的临时状态，供批量渲染使用。*/
var DocumentationBuilder.sectionsList: List<MutableMap<String, String>>? by createKey(DocumentationBuilder.Keys)

/** 初始化 [sectionsList]，创建固定大小的可变映射列表。*/
fun DocumentationBuilder.initSections(listSize: Int) {
    sectionsList = List(listSize) { mutableMapOf() }
}

/** 获取索引 [index] 对应的分节映射（越界返回 `null`）。*/
fun DocumentationBuilder.getSections(index: Int): MutableMap<String, String>? {
    return sectionsList?.getOrNull(index)
}

/**
 * 根据 [sectionsList] 批量构建分节输出。
 *
 * 若为空或未初始化则不输出任何内容。
 */
fun DocumentationBuilder.buildSections() {
    val sectionsList = this.sectionsList
    if (sectionsList.isNullOrEmpty()) return
    sections {
        for (sections in sectionsList) {
            for ((key, value) in sections) {
                section(key, value)
            }
        }
    }
}
