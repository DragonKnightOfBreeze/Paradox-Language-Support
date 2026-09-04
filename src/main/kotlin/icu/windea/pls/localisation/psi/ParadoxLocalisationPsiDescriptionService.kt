package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or

object ParadoxLocalisationPsiDescriptionService {
     fun getName(element: PsiElement): String? {
        return when (element) {
            is ParadoxLocalisationProperty -> element.name
            else -> null
        }
    }

     fun getType(element: PsiElement): String? {
         // should not be upper-cased
        return when (element) {
            is ParadoxLocalisationProperty -> ChronicleBundle.message("localisation.description.type.property")
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
