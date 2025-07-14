package icu.windea.pls.csv.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.csv.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.settings.PlsInternalSettings
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
            is ParadoxCsvRow -> PlsStringConstants.row
            is ParadoxCsvColumn -> element.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit)
            else -> throw InternalError()
        }
    }
}
