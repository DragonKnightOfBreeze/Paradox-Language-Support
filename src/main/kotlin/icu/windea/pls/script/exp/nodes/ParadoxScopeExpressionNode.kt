package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

class ParadoxScopeExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	override val errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpressionNode {
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedScope", text))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeExpressionNode {
			val nodes = SmartList<ParadoxScriptExpressionNode>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			ParadoxSystemScopeExpressionNode.resolve(text, textRange, configGroup)?.let {
				nodes.add(it)
				return ParadoxScopeExpressionNode(text, textRange, nodes, errors)
			}
			ParadoxScopeLinkExpressionNode.resolve(text, textRange, configGroup)?.let {
				nodes.add(it)
				return ParadoxScopeExpressionNode(text, textRange, nodes, errors)
			}
			ParadoxScopeLinkFromDataExpressionNode.resolve(text, textRange, configGroup)?.let {
				nodes.add(it)
				return ParadoxScopeExpressionNode(text, textRange, nodes, errors)
			}
			if(text.isEmpty()) {
				val error = ParadoxMissingScopeExpressionError(textRange, PlsBundle.message("script.expression.missingScope"))
				errors.add(error)
			}
			return ParadoxScopeExpressionNode(text, textRange, nodes, errors)
		}
	}
}

