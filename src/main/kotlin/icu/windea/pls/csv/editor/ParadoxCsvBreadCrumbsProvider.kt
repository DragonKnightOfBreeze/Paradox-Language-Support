package icu.windea.pls.csv.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.constants.*

class ParadoxCsvBreadCrumbsProvider : BreadcrumbsProvider {
    private val _defaultLanguages = arrayOf(ParadoxCsvLanguage)

    override fun getLanguages(): Array<out Language?>? {
        return _defaultLanguages
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return element is ParadoxCsvRow || element is ParadoxCsvColumn
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is ParadoxCsvHeader -> PlsStringConstants.header
            is ParadoxCsvRow -> PlsStringConstants.row
            is ParadoxCsvColumn -> getPresentableText(element)
            else -> throw InternalError()
        }
    }

    private fun getPresentableText(column: ParadoxCsvColumn): String {
        return buildString {
            append(column.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit))
            val headerColumn = column.getHeaderColumn()
            if (headerColumn != null) {
                append(" (")
                append(headerColumn.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit))
                append(")")
            }
        }
    }
}
