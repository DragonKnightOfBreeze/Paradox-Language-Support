package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.quote
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.csv.psi.isEmptyColumn
import icu.windea.pls.lang.util.ParadoxCsvManager

/**
 * 将 CSV 文本渲染为纯文本的渲染器。
 *
 * 说明：
 * - 移除额外的注释、空行和空白，以及不必要的括起表达式的双引号。
 */
class ParadoxCsvTextPlainRenderer : ParadoxCsvTextRendererBase<ParadoxCsvTextPlainRenderer.Context, String>() {
    data class Context(
        var builder: StringBuilder = StringBuilder(),
        var started: Boolean = false,
    )

    override fun initContext(): Context {
        return Context()
    }

    override fun getOutput(context: Context): String {
        return context.builder.toString()
    }

    context(context: Context)
    override fun renderFile(element: ParadoxCsvFile) {
        val header = element.header
        if (header != null) {
            renderRowElement(header)
        }
        for (row in element.rows) {
            ProgressManager.checkCanceled()
            renderRowElement(row)
        }
    }

    context(context: Context)
    override fun renderRowElement(element: ParadoxCsvRowElement) {
        val columns = element.columnList
        if (columns.isEmpty() && !hasTrailingSeparator(element)) return

        if (context.started) context.builder.append('\n')
        context.started = true

        for ((index, column) in columns.withIndex()) {
            ProgressManager.checkCanceled()
            if (index != 0) context.builder.append(ParadoxCsvManager.getSeparator())
            renderColumn(column)
        }
        if (hasTrailingSeparator(element)) {
            context.builder.append(ParadoxCsvManager.getSeparator())
        }
    }

    context(context: Context)
    override fun renderColumn(element: ParadoxCsvColumn) {
        val text = getRenderedColumnText(element)
        context.builder.append(text)
    }

    private fun hasTrailingSeparator(element: ParadoxCsvRowElement): Boolean {
        return element.lastChild?.text == ParadoxCsvManager.getSeparator().toString()
    }

    private fun getRenderedColumnText(column: ParadoxCsvColumn): String {
        if (column.isEmptyColumn()) return ""

        val value = column.value

        val needQuoteBecauseBoundaryBlank = value.isNotEmpty() && (value.first().isWhitespace() || value.last().isWhitespace())
        if (needQuoteBecauseBoundaryBlank) return value.quote()

        val extraChars = ParadoxCsvManager.getSeparator().toString() + "#"
        return value.quoteIfNecessary(extraChars = extraChars, blank = false)
    }
}
