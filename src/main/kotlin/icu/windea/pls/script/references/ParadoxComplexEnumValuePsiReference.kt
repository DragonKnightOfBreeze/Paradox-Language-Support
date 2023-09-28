package icu.windea.pls.script.references

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*

class ParadoxComplexEnumValuePsiReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val info: ParadoxComplexEnumValueInfo,
    val project: Project
) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName))
    }
    
    override fun resolve(): PsiElement {
        return ParadoxComplexEnumValueElement(element, info, project)
    }
}