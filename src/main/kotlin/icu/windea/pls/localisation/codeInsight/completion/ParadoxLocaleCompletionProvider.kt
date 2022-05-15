package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

object ParadoxLocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//直到所在行开始没有任何空白，直到所在行结束没有除了冒号之外的任何其他字符
		val position = parameters.position
		if(position.elementType == ParadoxLocalisationElementTypes.PROPERTY_KEY_ID) {
			if(position.nextSibling != null || position.parent?.parent?.prevSibling?.let {
					it.elementType != TokenType.WHITE_SPACE || it.text.last().let { c -> c != '\n' && c != '\r' }
				} == true) return
		}
		val project = position.project
		for(locale in getInternalConfig(project).locales) {
			val element = locale.pointer.element ?: continue
			val typeText = locale.pointer.containingFile?.name ?: anonymousString
			val lookupElement = LookupElementBuilder.create(element, locale.id).withIcon(locale.icon)
				.withTypeText(typeText, true)
			result.addElement(lookupElement)
		}
	}
}