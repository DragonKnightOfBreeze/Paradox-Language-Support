package icu.windea.pls.lang.references

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.model.ParadoxComplexEnumValueInfo

class ParadoxComplexEnumValuePsiReference(
    element: ParadoxExpressionElement,
    rangeInElement: TextRange,
    val info: ParadoxComplexEnumValueInfo
) : PsiReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement {
        val readWriteAccess = ReadWriteAccessDetector.Access.Write // write (declaration)
        return ParadoxComplexEnumValueLightElement(element, info.name, info.enumName, readWriteAccess, info.gameType, info.project)
    }
}
