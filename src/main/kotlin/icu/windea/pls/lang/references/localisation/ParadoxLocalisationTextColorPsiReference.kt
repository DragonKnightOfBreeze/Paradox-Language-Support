package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextColorAwareElement

/**
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationColorCompletionProvider
 */
class ParadoxLocalisationTextColorPsiReference(
    element: ParadoxLocalisationTextColorAwareElement,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationTextColorAwareElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        val element = element
        when (element) {
            is ParadoxLocalisationColorfulText -> return element.setName(newElementName)
            else -> throw IncorrectOperationException() // 不支持重命名
        }
    }

    override fun resolve(): PsiElement? {
        return element.colorInfo?.element
    }
}
