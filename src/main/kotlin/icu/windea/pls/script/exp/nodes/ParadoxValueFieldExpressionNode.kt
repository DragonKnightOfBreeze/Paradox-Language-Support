package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
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
			val nodes = SmartList<ParadoxScriptExpressionNode>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			ParadoxValueLinkExpressionNode.resolve(text, textRange, configGroup)?.let { 
				nodes.add(it)
				return ParadoxValueFieldExpressionNode(text, textRange, nodes, errors)
			}
			ParadoxValueLinkFromDataExpressionNode.resolve(text, textRange, configGroup)?.let {
				nodes.add(it)
				return ParadoxValueFieldExpressionNode(text, textRange, nodes, errors)
			}
			if(text.isEmpty()) {
				val error = ParadoxMissingScopeExpressionError(textRange, PlsBundle.message("script.expression.missingValueField"))
				errors.add(error)
			}
			return ParadoxValueFieldExpressionNode(text, textRange, nodes, errors)
		}
	}
}

