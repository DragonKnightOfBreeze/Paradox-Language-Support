package icu.windea.pls.cwt.psi

import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import com.intellij.lang.findUsages.FindUsagesProvider

/**
 * @see ElementDescriptionProvider
 * @see FindUsagesProvider
 */
object CwtPsiDescriptionService {
    fun getName(element: PsiElement): String? {
        return when (element) {
            is CwtOption -> element.name
            is CwtProperty -> element.name
            is CwtString -> element.name
            else -> null
        }
    }

    fun getType(element: PsiElement): String? {
        // should not be upper-cased
        return when (element) {
            is CwtOption -> ChronicleBundle.message("cwt.description.type.option")
            is CwtProperty -> ChronicleBundle.message("cwt.description.type.property")
            is CwtString -> ChronicleBundle.message("cwt.description.type.string")
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
