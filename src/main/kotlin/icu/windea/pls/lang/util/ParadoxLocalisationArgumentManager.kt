package icu.windea.pls.lang.util

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*

object ParadoxLocalisationArgumentManager {
    fun getReferences(element: ParadoxLocalisationArgument): Array<out PsiReference> {
        return CachedValuesManager.getCachedValue(element) {
            val value = doGetReferences(element)
            CachedValueProvider.Result.create(value, element)
        }
    }

    private fun doGetReferences(element: ParadoxLocalisationArgument): Array<out PsiReference> {
        val references = mutableListOf<PsiReference>()
        val argumentText = element.text
        run {
            if (element !is ParadoxLocalisationTextColorAwareElement) return@run
            val i = argumentText.indexOfFirst { ParadoxTextColorManager.isIdInArgument(it) }
            if (i == -1) return@run
            val rangeInElement = TextRange(i, i + 1)
            val reference = ParadoxLocalisationTextColorPsiReference(element, rangeInElement)
            references += reference
        }
        if (references.isEmpty()) return PsiReference.EMPTY_ARRAY
        return references.toTypedArray()
    }
}
