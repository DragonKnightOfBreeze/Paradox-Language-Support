package icu.windea.pls.model.data.annotated

import icu.windea.pls.lang.data.annotated.ParadoxAnnotatedDataFactory
import icu.windea.pls.lang.util.renderers.ParadoxCsvTextAnnotatedRenderer
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextAnnotatedRenderer

/**
 * 注解数据。
 *
 * 用于提取、嵌入、渲染或检查关键语法信息和语义信息，例如类型和规则表达式。
 * 可以被渲染为文本或者特殊注释。
 *
 * 可以被用来实现基于注解数据的渲染器（annotatedRenderer），以进行快照测试（snapshotTest）。
 *
 * @see ParadoxAnnotatedLevel
 * @see ParadoxAnnotatedDataFactory
 * @see ParadoxScriptTextAnnotatedRenderer
 * @see ParadoxCsvTextAnnotatedRenderer
 */
interface ParadoxAnnotatedData {
    val name: String

    fun render(): String

    fun toComment(): String

    abstract class Base(override val name: String) : ParadoxAnnotatedData {
        // 3.0.2 `QuotePatterns.Default` should be used here to quote literal if needed
        abstract override fun render(): String

        override fun toComment(): String = "## @$name ${render()}"
    }
}
