package icu.windea.pls.core.expression

import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.expression.nodes.*

interface ParadoxScriptValueExpression : ParadoxComplexExpression {
	val scriptValueNode: ParadoxScriptValueExpressionNode
	val parameterNodes: List<ParadoxScriptValueParameterExpressionNode>
	
	//TODO
	
	companion object Resolver
}

fun ParadoxScriptValueExpression.Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null) : ParadoxScriptValueExpression {
	TODO()
}
