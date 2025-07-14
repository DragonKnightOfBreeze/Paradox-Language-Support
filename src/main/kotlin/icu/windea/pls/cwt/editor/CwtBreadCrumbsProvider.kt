package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.settings.PlsInternalSettings

class CwtBreadCrumbsProvider : BreadcrumbsProvider {
    private val _defaultLanguages = arrayOf(CwtLanguage)

    override fun getLanguages(): Array<out Language> {
        return _defaultLanguages
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return element is CwtProperty || (element is CwtValue && element.isBlockValue())
    }

    override fun getElementInfo(element: PsiElement): String {
        return when {
            element is CwtProperty -> element.name
            element is CwtValue -> element.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit)
            else -> throw InternalError()
        }
    }
}
