package icu.windea.pls.script.exp.nodes

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.exp.errors.*

interface ParadoxScriptExpressionNode {
	val text: String
	val rangeInExpression: TextRange
	
	val nodes: List<ParadoxScriptExpressionNode> get() = emptyList()
	val errors: List<ParadoxScriptExpressionError> get() = emptyList()
	
	fun getAttributesKey(): TextAttributesKey? = null
	
	fun getReference(element: PsiElement): PsiReference? = null
	
	fun getUnresolvedError(element: PsiElement): ParadoxScriptExpressionError? = null
}
