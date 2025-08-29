package icu.windea.pls.cwt.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue

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
            element is CwtValue -> element.name.truncateAndKeepQuotes(PlsFacade.getInternalSettings().presentableTextLengthLimit)
            else -> throw InternalError()
        }
    }
}
