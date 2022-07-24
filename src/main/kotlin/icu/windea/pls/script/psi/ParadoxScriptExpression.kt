package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * 脚本表达式。复杂的脚本表达式会拥有额外的表达式信息。
 */
interface ParadoxScriptExpression : PsiElement, ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost {
	override fun getValue(): String
}