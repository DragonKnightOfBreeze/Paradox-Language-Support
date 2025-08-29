package icu.windea.pls.localisation.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
    private val _defaultLanguages = arrayOf(ParadoxLocalisationLanguage)

    override fun getLanguages(): Array<out Language> {
        return _defaultLanguages
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return element is ParadoxLocalisationLocale || element is ParadoxLocalisationPropertyList || element is ParadoxLocalisationProperty
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is ParadoxLocalisationLocale -> element.name
            is ParadoxLocalisationPropertyList -> element.locale?.name.or.anonymous()
            is ParadoxLocalisationProperty -> element.name
            else -> throw InternalError()
        }
    }
}
