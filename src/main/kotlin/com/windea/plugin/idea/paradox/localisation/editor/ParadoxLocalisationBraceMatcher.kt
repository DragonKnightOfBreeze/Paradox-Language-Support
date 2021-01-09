package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationBraceMatcher : PairedBraceMatcher {
	companion object{
		private val bracePairs = arrayOf(
			BracePair(ParadoxLocalisationTypes.COMMAND_START, ParadoxLocalisationTypes.COMMAND_END, true),
			BracePair(ParadoxLocalisationTypes.COLORFUL_TEXT_START, ParadoxLocalisationTypes.COLORFUL_TEXT_END, true),
			BracePair(ParadoxLocalisationTypes.ICON_START, ParadoxLocalisationTypes.ICON_END, true),
			BracePair(ParadoxLocalisationTypes.PROPERTY_REFERENCE_START, ParadoxLocalisationTypes.PROPERTY_REFERENCE_END, true),
			BracePair(ParadoxLocalisationTypes.SERIAL_NUMBER_START, ParadoxLocalisationTypes.SERIAL_NUMBER_END, true)
		)
	}

	override fun getPairs(): Array<BracePair> = bracePairs

	override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

	override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
