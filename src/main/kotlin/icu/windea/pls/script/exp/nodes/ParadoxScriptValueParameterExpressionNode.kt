package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptValueParameterExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.ARGUMENT_KEY
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup) {
			TODO()
		}
	}
}
