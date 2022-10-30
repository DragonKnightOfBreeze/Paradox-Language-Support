package icu.windea.pls.script.expression

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.script.highlighter.*

sealed class ParadoxScriptTokenExpressionInfo(
	text: String,
	textRange: TextRange
) : ParadoxScriptExpressionInfo(text, textRange)

class ParadoxScriptOperatorExpressionInfo(
	text: String,
	textRange: TextRange
) : ParadoxScriptTokenExpressionInfo(text, textRange) {
	override fun getAttributesKey(): TextAttributesKey {
		return ParadoxScriptAttributesKeys.OPERATOR_KEY
	}
}

class ParadoxScriptMarkerExpressionInfo(
	text: String,
	textRange: TextRange
) : ParadoxScriptTokenExpressionInfo(text, textRange) {
	override fun getAttributesKey(): TextAttributesKey {
		return ParadoxScriptAttributesKeys.MARKER_KEY
	}
}