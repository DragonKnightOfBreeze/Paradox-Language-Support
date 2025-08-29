package icu.windea.pls.script.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

class ParadoxScriptBreadCrumbsProvider : BreadcrumbsProvider {
    private val _defaultLanguages = arrayOf(ParadoxScriptLanguage)

    override fun getLanguages(): Array<out Language> {
        return _defaultLanguages
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty
            || (element is ParadoxScriptValue && element.isBlockMember())
            || element is ParadoxScriptScriptedVariable
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is ParadoxScriptProperty -> element.name
            is ParadoxScriptString -> element.name
            is ParadoxScriptValue -> element.name.truncateAndKeepQuotes(PlsFacade.getInternalSettings().presentableTextLengthLimit)
            is ParadoxScriptScriptedVariable -> element.name ?: PlsStringConstants.unresolved
            else -> throw InternalError()
        }
    }
}
