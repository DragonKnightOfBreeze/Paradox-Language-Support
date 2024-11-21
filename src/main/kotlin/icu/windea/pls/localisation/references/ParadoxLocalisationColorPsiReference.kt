package icu.windea.pls.localisation.references

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.localisation.psi.*

/**
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationColorCompletionProvider
 */
class ParadoxLocalisationColorPsiReference(
    element: ParadoxLocalisationColorfulText,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationColorfulText>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    override fun resolve(): PsiElement? {
        return element.colorConfig?.pointer?.element
    }

    fun getAttributesKey(): TextAttributesKey? {
        return element.colorConfig?.color?.let { ParadoxLocalisationAttributesKeys.getColorKey(it) }
    }
}
