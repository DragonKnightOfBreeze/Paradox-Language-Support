package icu.windea.pls.script.expression

import com.intellij.psi.ContributedReferenceHost
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralValue
import icu.windea.pls.script.psi.*

/**
 * 脚本表达式。复杂的脚本表达式会拥有额外的表达式信息。
 */
interface ParadoxScriptExpression : PsiElement, ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost {
	override fun getValue(): String
	
	//val expressionInfo: ParadoxScriptExpressionInfo? get() = null
}