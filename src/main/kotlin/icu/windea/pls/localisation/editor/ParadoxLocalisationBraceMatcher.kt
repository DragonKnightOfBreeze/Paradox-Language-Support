package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationBraceMatcher : PairedBraceMatcher {
	companion object{
		private val bracePairs = arrayOf(
			BracePair(PROPERTY_REFERENCE_START, PROPERTY_REFERENCE_END,false),
			BracePair(ICON_START, ICON_END,false),
			BracePair(SEQUENTIAL_NUMBER_START, SEQUENTIAL_NUMBER_END,false),
			BracePair(COMMAND_START, COMMAND_END,false),
			BracePair(COLORFUL_TEXT_START, COLORFUL_TEXT_END,false)
		)
	}

	override fun getPairs(): Array<BracePair> = bracePairs

	override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

	override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
