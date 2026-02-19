package icu.windea.pls.lang.references.script

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxComplexEnumValuePsiReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val info: ParadoxComplexEnumValueInfo
) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement {
        val readWriteAccess = Access.Write // write (declaration)
        return ParadoxComplexEnumValueElement(element, info.name, info.enumName, readWriteAccess, info.gameType, info.project)
    }
}
