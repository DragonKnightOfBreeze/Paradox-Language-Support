package icu.windea.pls.csv.codeInsight

import com.intellij.codeInsight.hint.DeclarationRangeHandler
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.siblings
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow

/**
 * 用于在 CSV 文件中，提供上下文信息（表格头）。
 */
class ParadoxCsvDeclarationRangeHandler : DeclarationRangeHandler<ParadoxCsvRow> {
    override fun getDeclarationRange(container: ParadoxCsvRow): TextRange? {
        val header = container.parent?.firstChild?.siblings()?.findIsInstance<ParadoxCsvHeader>()
        return header?.textRange
    }
}
