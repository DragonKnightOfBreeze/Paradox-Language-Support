package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationColorCompletionProvider
 */
class ParadoxLocalisationTextColorPsiReference(
    element: ParadoxLocalisationTextColorAwareElement,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationTextColorAwareElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        val element = element
        if (element !is ParadoxLocalisationColorfulText) return element
        return element.setName(newElementName)
    }

    override fun resolve(): PsiElement? {
        return element.colorInfo?.pointer?.element
    }
}
