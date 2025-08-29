package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.util.ParadoxLocalisationArgumentManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression

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

    private fun getReference(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterPsiReference? {
        val rangeInElement = element.idElement?.textRangeInParent ?: return null
        return ParadoxLocalisationParameterPsiReference(element, rangeInElement)
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
