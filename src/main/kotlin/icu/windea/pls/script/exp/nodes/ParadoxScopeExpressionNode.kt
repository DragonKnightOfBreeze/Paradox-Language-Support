package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

class ParadoxScopeExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	val canResolve: Boolean = true
) : ParadoxScriptExpressionNode {
	override fun getUnresolvedError(element: PsiElement): ParadoxScriptExpressionError? {
		if(canResolve) return null
		if(text.isEmpty()) return null
		return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedScopeExpression", text))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeExpressionNode {
			ParadoxSystemScopeExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxScopeExpressionNode(text, textRange, it.toSingletonList()) }
			ParadoxScopeLinkExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxScopeExpressionNode(text, textRange, it.toSingletonList()) }
			ParadoxScopeLinkFromDataExpressionNode.resolve(text, textRange, configGroup)
				?.let { return ParadoxScopeExpressionNode(text, textRange, it.toSingletonList()) }
			if(text.isParameterAwareExpression()) return ParadoxScopeExpressionNode(text, textRange)
			return ParadoxScopeExpressionNode(text, textRange, canResolve = false)
		}
	}
}
