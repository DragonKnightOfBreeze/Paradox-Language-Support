package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.ui.breadcrumbs.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

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
            is ParadoxScriptValue -> element.name
            is ParadoxScriptScriptedVariable -> element.name ?: PlsConstants.unresolvedString
            else -> throw InternalError()
        }
    }
}
