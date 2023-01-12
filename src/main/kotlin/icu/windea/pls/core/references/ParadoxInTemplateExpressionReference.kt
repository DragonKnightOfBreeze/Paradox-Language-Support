package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

class ParadoxInTemplateExpressionReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val name: String,
    val configExpression: CwtDataExpression,
    val configGroup: CwtConfigGroup
) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), PsiNodeReference {
    override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
       throw IncorrectOperationException() // cannot rename
    }
    
    override fun resolve(): PsiElement? {
        return resolve(true)
    }
    
    override fun resolve(exact: Boolean): PsiElement? {
        val element = element
        return CwtConfigHandler.resolveScriptExpression(element, rangeInElement, null, configExpression, configGroup, exact = exact)
    }
    
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val element = element
        return CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, null, configExpression, configGroup)
            .mapToArray { PsiElementResolveResult(it) }
    }
}