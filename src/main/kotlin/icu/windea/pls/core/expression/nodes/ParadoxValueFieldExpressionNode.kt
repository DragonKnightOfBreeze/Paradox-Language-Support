package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.script.psi.*

open class ParadoxValueFieldExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxScriptExpressionNode {
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		return ParadoxUnresolvedScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", text))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpressionNode {
			ParadoxValueLinkExpressionNode.resolve(text, textRange, configGroup)?.let { return it }
			ParadoxValueLinkFromDataExpressionNode.resolve(text, textRange, configGroup)?.let {return it }
			return ParadoxValueFieldExpressionNode(text, textRange)
		}
	}
}

