package icu.windea.pls.csv.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.icon
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.getHeaderColumn
import icu.windea.pls.model.constants.PlsStringConstants
import javax.swing.Icon

class ParadoxCsvNavBar : StructureAwareNavBarModelExtension() {
    override val language: Language = ParadoxCsvLanguage

    override fun getIcon(o: Any?): Icon? {
        return when {
            o is PsiElement -> o.icon
            else -> null
        }
    }

    override fun getPresentableText(o: Any?): String? {
        return when {
            o is ParadoxCsvHeader -> PlsStringConstants.headerMarker
            o is ParadoxCsvRow -> PlsStringConstants.rowMarker
            o is ParadoxCsvColumn -> getPresentableText(o)
            else -> null
        }
    }

    private fun getPresentableText(column: ParadoxCsvColumn): String {
        val limit = PlsFacade.getInternalSettings().presentableTextLengthLimit
        return buildString {
            append(column.name.truncateAndKeepQuotes(limit))
            val headerColumn = column.getHeaderColumn()
            if (headerColumn != null) {
                append(" (")
                append(headerColumn.name.truncateAndKeepQuotes(limit))
                append(")")
            }
        }
    }
}
