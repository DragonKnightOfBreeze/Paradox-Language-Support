package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.cwt.psi.CwtPsiDescriptionService

object ParadoxScriptPsiDescriptionService {
     fun getName(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptScriptedVariable -> element.name
            is ParadoxScriptProperty -> element.name
            else -> null
        }
    }

     fun getType(element: PsiElement): String? {
         // should not be upper-cased
        return when (element) {
            is ParadoxScriptScriptedVariable -> ChronicleBundle.message("script.description.type.scriptedVariable")
            is ParadoxScriptProperty -> ChronicleBundle.message("script.description.type.property")
            else -> null
        }
    }

    fun getNodeText(element: PsiElement): String? {
        // {type} {nameOrAnonymous}
        val type = getType(element) ?: return null
        val name = getName(element)
        return type + " " + name.or.anonymous()
    }

    fun getHighlightUsagesDescription(element: PsiElement): String? {
        return getNodeText(element)
    }
}
