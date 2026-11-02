package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

object ParadoxLocalisationElementDescriptionService {
    fun getName(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return when (element) {
            is ParadoxLocalisationProperty -> element.name
            else -> null
        }
    }

    fun getType(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // should not be upper-cased
        return when (element) {
            is ParadoxLocalisationProperty -> ChronicleBundle.message("localisation.description.type.property")
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

    private fun checkLanguage(element: PsiElement): Boolean = element.language == ParadoxLocalisationLanguage
}
