package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptBraceMatcher : PairedBraceMatcher {
	private val _bracePairs = arrayOf(
		BracePair(LEFT_BRACE, RIGHT_BRACE, true),
		BracePair(PARAMETER_START, PARAMETER_END, false),
		BracePair(LEFT_BRACKET, RIGHT_BRACKET, false), //cannot be structural
		BracePair(NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET, false), //cannot be structural
		BracePair(INLINE_MATH_START, INLINE_MATH_END, true),
		BracePair(LABS_SIGN, RABS_SIGN, false),
		BracePair(LP_SIGN, RP_SIGN, true)
	)
	
	override fun getPairs(): Array<BracePair> = _bracePairs
	
	override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
	
	override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
