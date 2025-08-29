package icu.windea.pls.csv.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.getHeaderColumn
import icu.windea.pls.model.constants.PlsStringConstants

class ParadoxCsvBreadCrumbsProvider : BreadcrumbsProvider {
    private val _defaultLanguages = arrayOf(ParadoxCsvLanguage)

    override fun getLanguages(): Array<out Language> {
        return _defaultLanguages
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return element is ParadoxCsvRow || element is ParadoxCsvColumn
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is ParadoxCsvHeader -> PlsStringConstants.headerMarker
            is ParadoxCsvRow -> PlsStringConstants.rowMarker
            is ParadoxCsvColumn -> getPresentableText(element)
            else -> throw InternalError()
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
