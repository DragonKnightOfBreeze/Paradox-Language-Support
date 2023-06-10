package icu.windea.pls.script.surroundWith

import com.intellij.lang.surroundWith.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

//com.intellij.json.surroundWith.JsonSurroundDescriptor
//com.intellij.json.surroundWith.JsonSurrounderBase
//com.intellij.json.surroundWith.JsonWithObjectLiteralSurrounder

class ParadoxScriptSurroundDescriptor : SurroundDescriptor {
	companion object {
		private val defaultSurrounders = arrayOf(
			ParadoxScriptClausePropertySurrounder(),
			ParadoxScriptClauseSurrounder(),
			ParadoxScriptParameterConditionSurrounder()
		)
	}
	
	override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<PsiElement> {
		return file.findElementsBetween(startOffset, endOffset, { it.parentOfType<ParadoxScriptBlockElement>() }) {
			it
		}.toTypedArray()
	}
	
	override fun getSurrounders(): Array<Surrounder> {
		return defaultSurrounders
	}
	
	override fun isExclusive(): Boolean {
		return false
	}
}
