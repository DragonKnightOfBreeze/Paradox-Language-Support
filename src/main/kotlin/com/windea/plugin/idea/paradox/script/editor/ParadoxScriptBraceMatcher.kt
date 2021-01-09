package com.windea.plugin.idea.paradox.script.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptBraceMatcher : PairedBraceMatcher {
	companion object{
		private val bracePairs = arrayOf(
			BracePair(ParadoxScriptTypes.LEFT_BRACE, ParadoxScriptTypes.RIGHT_BRACE, true),
			BracePair(ParadoxScriptTypes.CODE_START, ParadoxScriptTypes.CODE_END, true)
		)
	}

	override fun getPairs(): Array<BracePair> = bracePairs

	override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

	override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
