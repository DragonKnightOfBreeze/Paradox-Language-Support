package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

class ParadoxScopeExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	override val errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpressionNode {
	val prefixNode = nodes.singleOrNull()?.castOrNull<ParadoxScopeLinkFromDataExpressionNode>()
		?.nodes?.getOrNull(0)?.castOrNull<ParadoxScopeLinkPrefixExpressionNode>()
	
	val dataSourceNode = nodes.singleOrNull()?.castOrNull<ParadoxScopeLinkFromDataExpressionNode>()
		?.nodes?.getOrNull(0)?.castOrNull<ParadoxScopeLinkDataSourceExpressionNode>()
	
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedScope", text))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeExpressionNode {
			ParadoxSystemScopeExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxScopeExpressionNode(text, textRange, it.toSingletonList()) }
			ParadoxScopeLinkExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxScopeExpressionNode(text, textRange, it.toSingletonList()) }
			ParadoxScopeLinkFromDataExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxScopeExpressionNode(text, textRange, it.toSingletonList()) }
			if(text.isEmpty()) {
				val error = ParadoxMissingScopeExpressionError(textRange, PlsBundle.message("script.expression.missingScope"))
				return ParadoxScopeExpressionNode(text, textRange, emptyList(), error.toSingletonListOrEmpty())
			}
			return ParadoxScopeExpressionNode(text, textRange)
		}
	}
}

