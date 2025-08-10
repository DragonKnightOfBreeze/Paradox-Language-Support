package icu.windea.pls.csv.navigation

import com.intellij.ide.navigationToolbar.*
import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.model.constants.*
import javax.swing.*

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
            o is ParadoxCsvHeader -> PlsStringConstants.header
            o is ParadoxCsvRow -> PlsStringConstants.row
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
