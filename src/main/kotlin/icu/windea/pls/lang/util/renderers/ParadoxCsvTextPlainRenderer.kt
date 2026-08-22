package icu.windea.pls.lang.util.renderers

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.lang.psi.formattedValue

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
        element.rows.forEachFast { row ->
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
        columns.forEachFast { column ->
            ProgressManager.checkCanceled()
            if (m.mark()) renderSeparator()
            renderColumn(column)
        }
        if (hasTrailingSeparator(element)) renderSeparator()
    }

    override fun renderColumn(element: ParadoxCsvColumn) {
        val v = element.formattedValue()
        builder.append(v)
    }

    fun renderSeparator() {
        builder.append(settings.separator)
    }

    fun hasTrailingSeparator(element: ParadoxCsvColumnContainer): Boolean {
        return element.lastChild?.text == ParadoxCsvPsiService.getSeparator().toString()
    }
}

data class ParadoxCsvTextPlainRenderSettings(
    var separator: String = ";",
) : ParadoxCsvTextRenderSettings()
