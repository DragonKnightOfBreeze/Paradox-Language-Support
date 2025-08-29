package icu.windea.pls.lang.references.localisation

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale

/**
 * @see icu.windea.pls.lang.codeInsight.completion.localisation.ParadoxLocalisationLocaleCompletionProvider
 */
class ParadoxLocalisationLocalePsiReference(
    element: ParadoxLocalisationLocale,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationLocale>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException() //cannot rename locale
    }

    override fun resolve(): PsiElement? {
        return selectLocale(element)?.pointer?.element
    }
}
