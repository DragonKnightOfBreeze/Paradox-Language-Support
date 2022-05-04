package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.TokenType
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes

object LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
	private val localeElements = getInternalConfig().locales.map {
		LookupElementBuilder.create(it.id).withIcon(it.icon).withTailText(it.tailText, true)
	}
	
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val position = parameters.position
		if(position.elementType == ParadoxLocalisationElementTypes.PROPERTY_KEY_ID) {
			if(position.nextSibling != null || position.parent?.parent?.prevSibling?.let {
					it.elementType != TokenType.WHITE_SPACE || it.text.last().let { c -> c != '\n' && c != '\r' }
				} != false) return
		}
		result.addAllElements(localeElements)
	}
}