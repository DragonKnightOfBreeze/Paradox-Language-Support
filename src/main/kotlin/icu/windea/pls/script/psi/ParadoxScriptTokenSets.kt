package icu.windea.pls.script.psi

import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

object ParadoxScriptTokenSets {
	val comparisonTokens = TokenSet.create(LT_SIGN, GT_SIGN, LE_SIGN, GE_SIGN, NOT_EQUAL_SIGN)
	val scriptedVariableTokens = TokenSet.create(BOOLEAN_TOKEN, INT_TOKEN, FLOAT_TOKEN)
	val parameterValueTokens = TokenSet.create(BOOLEAN_TOKEN, INT_TOKEN, FLOAT_TOKEN)
	val inlineMathParameterValueTokens = TokenSet.create(INT_TOKEN, FLOAT_TOKEN)
}
