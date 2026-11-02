package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.script.ParadoxScriptLanguage

object ParadoxScriptElementDescriptionService {
    fun getName(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return when (element) {
            is ParadoxScriptScriptedVariable -> element.name
            is ParadoxScriptProperty -> element.name
            else -> null
        }
    }

    fun getType(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // should not be upper-cased
        return when (element) {
            is ParadoxScriptScriptedVariable -> ChronicleBundle.message("script.description.type.scriptedVariable")
            is ParadoxScriptProperty -> ChronicleBundle.message("script.description.type.property")
            else -> null
        }
    }

    fun getNodeText(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // {type} {nameOrAnonymous}
        val type = getType(element) ?: return null
        val name = getName(element)
        return type + " " + name.orAnonymous()
    }

    fun getHighlightUsagesDescription(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return getNodeText(element)
    }

    private fun checkLanguage(element: PsiElement): Boolean = element.language == ParadoxScriptLanguage
}
