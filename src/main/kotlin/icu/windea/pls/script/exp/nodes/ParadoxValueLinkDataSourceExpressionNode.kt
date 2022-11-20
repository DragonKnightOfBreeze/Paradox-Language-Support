package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*

class ParadoxValueLinkDataSourceExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxScriptExpressionNode {
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueLinkDataSourceExpressionNode? {
			TODO()
		}
	}
}
