package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element is ParadoxLocalisationArgument) return ParadoxLocalisationArgumentManager.getReferences(element)

        val reference = when (element) {
            is ParadoxLocalisationLocale -> getReference(element)
            is ParadoxLocalisationColorfulText -> getReference(element)
            is ParadoxLocalisationParameter -> getReference(element)
            is ParadoxLocalisationIcon -> getReference(element)
            is ParadoxLocalisationConceptCommand -> getReference(element)
            is ParadoxLocalisationTextFormat -> getReference(element)
            is ParadoxLocalisationTextIcon -> getReference(element)
            is ParadoxLocalisationScriptedVariableReference -> getReference(element)
            else -> null
        }
        if (reference == null) return PsiReference.EMPTY_ARRAY
        return arrayOf(reference)
    }

    private fun getReference(element: ParadoxLocalisationLocale): ParadoxLocalisationLocalePsiReference {
        val rangeInElement = element.idElement.textRangeInParent
        return ParadoxLocalisationLocalePsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationColorfulText): ParadoxLocalisationTextColorPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxLocalisationTextColorPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationParameter): ParadoxLocalisationPropertyPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxLocalisationPropertyPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationIcon): ParadoxLocalisationIconPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxLocalisationIconPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationConceptCommand): ParadoxLocalisationConceptPsiReference? {
        val nameElement = element.conceptName ?: return null
        //作为复杂表达式的场合，另行处理（参见：ParadoxLocalisationReferenceContributor）
        if (nameElement.isDatabaseObjectExpression(strict = true)) return null
        val rangeInElement = nameElement.textRangeInParent
        return ParadoxLocalisationConceptPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationTextFormat): ParadoxLocalisationTextFormatPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxLocalisationTextFormatPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationTextIcon): ParadoxLocalisationTextIconPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxLocalisationTextIconPsiReference(element, rangeInElement)
    }

    private fun getReference(element: ParadoxLocalisationScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxScriptedVariablePsiReference(element, rangeInElement)
    }
}
