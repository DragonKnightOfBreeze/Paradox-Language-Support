package icu.windea.pls.script.references

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*

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
        return ParadoxComplexEnumValueElement(element, info, project)
    }
}
