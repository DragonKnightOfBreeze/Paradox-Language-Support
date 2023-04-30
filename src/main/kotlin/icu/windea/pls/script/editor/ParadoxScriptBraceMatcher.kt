package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptBraceMatcher : PairedBraceMatcher {
	companion object {
		private val bracePairs = arrayOf(
			BracePair(LEFT_BRACE, RIGHT_BRACE, true),
			BracePair(KEY_PARAMETER_START, KEY_PARAMETER_END, false),
			BracePair(VALUE_PARAMETER_START, VALUE_PARAMETER_END, false),
			BracePair(INLINE_MATH_PARAMETER_START, INLINE_MATH_PARAMETER_END, false),
			BracePair(LEFT_BRACKET, RIGHT_BRACKET, false), //cannot be structural
			BracePair(NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET, false), //cannot be structural
			BracePair(INLINE_MATH_START, INLINE_MATH_END, true),
			BracePair(LABS_SIGN, RABS_SIGN, false),
			BracePair(LP_SIGN, RP_SIGN, true)
		)
	}
	
	override fun getPairs(): Array<BracePair> = bracePairs
	
	override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
	
	override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
