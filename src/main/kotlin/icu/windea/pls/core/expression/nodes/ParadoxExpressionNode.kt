package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.script.psi.*

interface ParadoxExpressionNode {
	val text: String
	val rangeInExpression: TextRange
	val nodes: List<ParadoxExpressionNode> get() = emptyList()
	
	fun getAttributesKey(): TextAttributesKey? = null
	
	fun getAttributesKeyConfig(element: ParadoxScriptExpressionElement): CwtConfig<*>? = null
	
	fun getReference(element: ParadoxScriptExpressionElement): PsiReference? = null
	
	fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxExpressionError? = null
}

