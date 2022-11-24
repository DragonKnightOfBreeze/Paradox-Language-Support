package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

class ParadoxValueFieldExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	override val errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpressionNode {
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", text))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpressionNode {
			ParadoxValueLinkExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxValueFieldExpressionNode(text, textRange, it.toSingletonList()) }
			ParadoxValueLinkFromDataExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxValueFieldExpressionNode(text, textRange, it.toSingletonList()) }
			if(text.isEmpty()) {
				val error = ParadoxMissingScopeExpressionError(textRange, PlsBundle.message("script.expression.missingValueField"))
				return ParadoxValueFieldExpressionNode(text, textRange, emptyList(), error.toSingletonListOrEmpty())
			}
			return ParadoxValueFieldExpressionNode(text, textRange)
		}
	}
}

