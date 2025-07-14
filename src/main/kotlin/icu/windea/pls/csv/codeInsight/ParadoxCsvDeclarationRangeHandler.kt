package icu.windea.pls.csv.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.csv.psi.*

/**
 * CSV的上下文信息（表格头）。
 */
class ParadoxCsvDeclarationRangeHandler : DeclarationRangeHandler<ParadoxCsvRow> {
    override fun getDeclarationRange(container: ParadoxCsvRow): TextRange? {
        val header = container.parent?.firstChild?.siblings()?.findIsInstance<ParadoxCsvHeader>()
        return header?.textRange
    }
}
