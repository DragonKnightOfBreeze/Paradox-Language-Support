package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.quote
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService

/**
 * 将 CSV 文本渲染为纯文本的渲染器。
 *
 * 说明：
 * - 移除额外的注释、空行和空白，以及不必要的括起表达式的双引号。
 */
class ParadoxCsvTextPlainRenderer : ParadoxCsvTextRenderer<String, ParadoxCsvTextPlainRenderSettings, ParadoxCsvTextPlainRenderContext>() {
    override val settings = ParadoxCsvTextPlainRenderSettings()

    override fun createContext() = ParadoxCsvTextPlainRenderContext(settings)
}

open class ParadoxCsvTextPlainRenderContext(
    private val settings: ParadoxCsvTextPlainRenderSettings,
    var builder: StringBuilder = StringBuilder(),
) : ParadoxCsvTextRenderContext<String>() {
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
            renderColumnContainer(header)
        }
        for (row in element.rows) {
            ProgressManager.checkCanceled()
            if (m.mark()) {
                builder.append('\n')
            }
            renderColumnContainer(row)
        }
    }

    override fun renderColumnContainer(element: ParadoxCsvColumnContainer) {
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

    fun renderSeparator() {
        builder.append(settings.separator)
    }

    fun getColumnText(column: ParadoxCsvColumn): String {
        if (ParadoxCsvPsiService.isEmptyColumn(column)) return ""

        val value = column.value

        val needQuoteBecauseBoundaryBlank = value.isNotEmpty() && (value.first().isWhitespace() || value.last().isWhitespace())
        if (needQuoteBecauseBoundaryBlank) return value.quote()

        val extraChars = ParadoxCsvPsiService.getSeparator().toString() + "#"
        return value.quoteIfNeeded(containAnyChar = extraChars, containBlank = false)
    }

    fun hasTrailingSeparator(element: ParadoxCsvColumnContainer): Boolean {
        return element.lastChild?.text == ParadoxCsvPsiService.getSeparator().toString()
    }
}

data class ParadoxCsvTextPlainRenderSettings(
    var separator: String = ";",
) : ParadoxCsvTextRenderSettings()
