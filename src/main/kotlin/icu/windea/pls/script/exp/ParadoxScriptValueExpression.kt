package icu.windea.pls.script.exp

import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.exp.nodes.*

interface ParadoxScriptValueExpression : ParadoxScriptComplexExpression {
	val scriptValueNode: ParadoxScriptValueExpressionNode
	val parameterNodes: List<ParadoxScriptValueParameterExpressionNode>
	
	//TODO
	
	companion object Resolver
}

fun ParadoxScriptValueExpression.Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null) : ParadoxScriptValueExpression {
	TODO()
}
