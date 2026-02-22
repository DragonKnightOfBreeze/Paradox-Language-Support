package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.lang.codeInsight.ParadoxTypeManager
import icu.windea.pls.model.ParadoxType

/**
 * 将 CSV 文本渲染为带注解的文本的渲染器。
 *
 * 说明：
 * - 可以配置详细的注解级别。参见 [ParadoxRendererAnnotatedLevel]。
 *
 * 支持的注解：
 * - 类型信息：`## type = x`, `## type = { key = x value = x }`
 * - 规则表达式：`## config_expression = x`, `## config_expression = { key = x value = y }`
 */
class ParadoxCsvTextAnnotatedRenderer : ParadoxRenderer<ParadoxCsvTextAnnotatedRenderer.Context, String> {
    data class Context(
        var builder: StringBuilder = StringBuilder()
    )

    var level: ParadoxRendererAnnotatedLevel = ParadoxRendererAnnotatedLevel.DEFAULT

    override fun initContext(): Context {
        return Context()
    }

    override fun render(input: PsiElement, context: Context): String {
        return when (input) {
            is ParadoxCsvFile -> render(input, context)
            is ParadoxCsvRowElement -> render(input, context)
            is ParadoxCsvColumn -> render(input, context)
            else -> throw UnsupportedOperationException("Unsupported element type: ${input.elementType}")
        }
    }

    fun render(element: ParadoxCsvFile, context: Context = initContext()): String {
        ProgressManager.checkCanceled()
        renderHeader(element.header?.columnList, element.rows.firstOrNull()?.columnList, context)
        val body = ParadoxCsvTextPlainRenderer().render(element)
        if (context.builder.isNotEmpty() && body.isNotEmpty()) {
            context.builder.append("\n\n")
        }
        context.builder.append(body)
        return context.builder.toString()
    }

    fun render(element: ParadoxCsvRowElement, context: Context = initContext()): String {
        ProgressManager.checkCanceled()
        renderHeader(element.columnList, null, context)
        val body = ParadoxCsvTextPlainRenderer().render(element)
        if (context.builder.isNotEmpty() && body.isNotEmpty()) {
            context.builder.append("\n\n")
        }
        context.builder.append(body)
        return context.builder.toString()
    }

    fun render(element: ParadoxCsvColumn, context: Context = initContext()): String {
        ProgressManager.checkCanceled()
        renderHeader(listOf(element), null, context)
        val body = ParadoxCsvTextPlainRenderer().render(element)
        if (context.builder.isNotEmpty() && body.isNotEmpty()) {
            context.builder.append("\n\n")
        }
        context.builder.append(body)
        return context.builder.toString()
    }

    private fun renderHeader(headerColumns: List<ParadoxCsvColumn>?, firstRowColumns: List<ParadoxCsvColumn>?, context: Context) {
        val columns = headerColumns ?: firstRowColumns
        if (columns.isNullOrEmpty()) return

        val headerLines = buildList {
            if (level.includeType) {
                val typeList = columns.indices.map { index ->
                    val column = firstRowColumns?.getOrNull(index) ?: headerColumns?.getOrNull(index)
                    val type = column?.let { ParadoxTypeManager.getType(it) } ?: ParadoxType.Unknown
                    type.id
                }
                add("## type = { ${typeList.joinToString(" ")} }")
            }
            if (level.includeConfigExpression) {
                val configExpressionList = columns.indices.map { index ->
                    val column = headerColumns?.getOrNull(index) ?: firstRowColumns?.getOrNull(index)
                    column?.let { ParadoxTypeManager.getConfigExpression(it) } ?: ParadoxType.Unknown.id
                }
                add("## config_expression = { ${configExpressionList.joinToString(" ")} }")
            }
        }

        if (headerLines.isEmpty()) return
        headerLines.forEachIndexed { index, line ->
            if (index != 0) context.builder.append('\n')
            context.builder.append(line)
        }
    }
}
