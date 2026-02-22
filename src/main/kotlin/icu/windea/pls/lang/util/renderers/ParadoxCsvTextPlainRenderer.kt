package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.quote
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.util.OnceMarker
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
class ParadoxCsvTextPlainRenderer : ParadoxCsvTextRenderer<ParadoxCsvTextPlainRenderer.Scope, String>() {
    override fun createScope(): Scope {
        return Scope()
    }

    open class Scope(
        var builder: StringBuilder = StringBuilder()
    ) : ParadoxCsvTextRenderer.Scope<String>() {
        override fun build(): String {
            return builder.toString()
        }

        override fun renderFile(element: ParadoxCsvFile) {
            val m = OnceMarker()
            val header = element.header
            if (header != null) {
                if (m.mark()) {
                    builder.append('\n')
                }
                renderRowElement(header)
            }
            for (row in element.rows) {
                ProgressManager.checkCanceled()
                if (m.mark()) {
                    builder.append('\n')
                }
                renderRowElement(row)
            }
        }

        override fun renderRowElement(element: ParadoxCsvRowElement) {
            val columns = element.columnList
            if (columns.isEmpty() && !hasTrailingSeparator(element)) return
            val m = OnceMarker()
            for (column in columns) {
                ProgressManager.checkCanceled()
                if (m.mark()) renderSeparator()
                renderColumn(column)
            }
            if (hasTrailingSeparator(element)) renderSeparator()
        }

        override fun renderColumn(element: ParadoxCsvColumn) {
            val text = getColumnText(element)
            builder.append(text)
        }

        protected fun renderSeparator() {
            builder.append(ParadoxCsvManager.getSeparator())
        }

        protected fun getColumnText(column: ParadoxCsvColumn): String {
            if (column.isEmptyColumn()) return ""

            val value = column.value

            val needQuoteBecauseBoundaryBlank = value.isNotEmpty() && (value.first().isWhitespace() || value.last().isWhitespace())
            if (needQuoteBecauseBoundaryBlank) return value.quote()

            val extraChars = ParadoxCsvManager.getSeparator().toString() + "#"
            return value.quoteIfNecessary(extraChars = extraChars, blank = false)
        }

        protected fun hasTrailingSeparator(element: ParadoxCsvRowElement): Boolean {
            return element.lastChild?.text == ParadoxCsvManager.getSeparator().toString()
        }
    }
}
