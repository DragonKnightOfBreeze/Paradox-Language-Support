package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

interface ParadoxScriptExpressionNode {
	val text: String
	val rangeInExpression: TextRange
	val nodes: List<ParadoxScriptExpressionNode> get() = emptyList()
	val errors: List<ParadoxScriptExpressionError> get() = emptyList()
	
	fun getAttributesKey(): TextAttributesKey? = null
	
	fun getAttributesKeyExpression(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>): CwtDataExpression? = null
	
	fun getReference(element: ParadoxScriptExpressionElement): PsiReference? = null
	
	fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionError? = null
}
