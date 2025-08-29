package icu.windea.pls.lang.references.script

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.indexInfo.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxComplexEnumValuePsiReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val info: ParadoxComplexEnumValueIndexInfo,
    val project: Project
) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement {
        val (name, enumName, readWriteAccess, _, gameType) = info
        return ParadoxComplexEnumValueElement(element, name, enumName, readWriteAccess, gameType, project)
    }
}
