package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationBreadCrumbsProvider : BreadcrumbsProvider {
    private val _defaultLanguages = arrayOf(ParadoxLocalisationLanguage)
    
    override fun getLanguages(): Array<out Language> {
        return _defaultLanguages
    }
    
    override fun acceptElement(element: PsiElement): Boolean {
        return element is ParadoxLocalisationLocale || element is ParadoxLocalisationPropertyList || element is ParadoxLocalisationProperty
    }
    
    override fun getElementInfo(element: PsiElement): String {
        return when(element) {
            is ParadoxLocalisationLocale -> element.name
            is ParadoxLocalisationPropertyList -> element.locale?.name.orAnonymous()
            is ParadoxLocalisationProperty -> element.name
            else -> throw InternalError()
        }
    }
}
