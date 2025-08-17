package icu.windea.pls.lang.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.resolveLocalisation
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.model.ParadoxLocalisationType

object ParadoxLocalisationTypeManager {
    fun isTypedElement(element: PsiElement): Boolean {
        if (element.language !is ParadoxBaseLanguage) return false
        return when (element) {
            is ParadoxLocalisationPropertyKey -> {
                val localisationElement = element.parent?.castOrNull<ParadoxLocalisationProperty>()
                localisationElement?.type != null
            }
            is ParadoxLocalisationParameter -> {
                val localisationElement = element.resolveLocalisation()
                localisationElement?.type != null
            }
            else -> false
        }
    }

    fun findTypedElements(elementAt: PsiElement): List<PsiElement> {
        if (elementAt.language !is ParadoxBaseLanguage) return emptyList()
        val element = elementAt.parents(withSelf = true).find { isTypedElement(it) }
        if (element == null) return emptyList()
        return listOf(element)
    }

    fun getType(element: PsiElement): ParadoxLocalisationType? {
        return when (element) {
            is ParadoxLocalisationPropertyKey -> element.parent?.castOrNull<ParadoxLocalisationProperty>()?.type
            is ParadoxLocalisationParameter -> element.resolveLocalisation()?.type
            else -> null
        }
    }
}
