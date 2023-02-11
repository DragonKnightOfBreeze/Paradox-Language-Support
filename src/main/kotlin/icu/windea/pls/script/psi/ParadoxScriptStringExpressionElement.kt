package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, PsiLiteralValue, ContributedReferenceHost, NavigatablePsiElement {
    val stub: ParadoxScriptStringExpressionElementStub<*>?
    
    override val value: String
    
    override fun setValue(value: String): ParadoxScriptStringExpressionElement
}
