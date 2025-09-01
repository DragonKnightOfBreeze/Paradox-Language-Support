@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

/** 构建文档字符串的便捷方法。 */
inline fun buildDocumentation(builderAction: DocumentationBuilder.() -> Unit): String {
    val builder = DocumentationBuilder()
    builder.builderAction()
    return builder.content.toString()
}

/** 添加定义片段。 */
inline fun DocumentationBuilder.definition(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.DEFINITION_START)
    block(this)
    append(DocumentationMarkup.DEFINITION_END)
    return this
}

/** 添加内容片段。 */
inline fun DocumentationBuilder.content(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.CONTENT_START)
    block(this)
    append(DocumentationMarkup.CONTENT_END)
    return this
}

/** 添加章节容器片段。 */
inline fun DocumentationBuilder.sections(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.SECTIONS_START)
    block(this)
    append(DocumentationMarkup.SECTIONS_END)
    return this
}

/** 添加单个章节（标题-内容）。 */
inline fun DocumentationBuilder.section(title: CharSequence, value: CharSequence): DocumentationBuilder {
    append(DocumentationMarkup.SECTION_HEADER_START)
    append(title).append(": ")
    append(DocumentationMarkup.SECTION_SEPARATOR).append("<p>")
    append(value)
    append(DocumentationMarkup.SECTION_END)
    return this
}

/** 添加灰色文本片段。 */
inline fun DocumentationBuilder.grayed(block: DocumentationBuilder.() -> Unit): DocumentationBuilder {
    append(DocumentationMarkup.GRAYED_START)
    block(this)
    append(DocumentationMarkup.GRAYED_END)
    return this
}

/** 保存章节列表的临时存储。 */
var DocumentationBuilder.sectionsList: List<MutableMap<String, String>>? by createKey(DocumentationBuilder.Keys)

/** 初始化章节列表的容量。 */
fun DocumentationBuilder.initSections(listSize: Int) {
    sectionsList = List(listSize) { mutableMapOf() }
}

/** 获取指定索引位置的章节 Map。 */
fun DocumentationBuilder.getSections(index: Int): MutableMap<String, String>? {
    return sectionsList?.getOrNull(index)
}

/** 输出所有章节。 */
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
