package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, PsiLiteralValue, ContributedReferenceHost, NavigatablePsiElement {
    override val value: String
    
    override fun setValue(value: String): ParadoxScriptStringExpressionElement
}
