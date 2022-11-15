package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供语言区域名字的代码补全。
 */
class ParadoxLocalisationLocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//直到所在行开始没有任何空白，直到所在行结束没有除了冒号之外的任何其他字符
		val position = parameters.position
		if(position.elementType == ParadoxLocalisationElementTypes.PROPERTY_KEY_ID) {
			if(position.nextSibling != null || position.parent?.parent?.prevSibling?.let {
					it.elementType != TokenType.WHITE_SPACE || it.text.last().let { c -> c != '\n' && c != '\r' }
				} == true) return
		}
		val file = parameters.originalFile
		val project = file.project
		val localeIdFromFileName = file.castOrNull<ParadoxLocalisationFile>()?.getLocaleIdFromFileName()
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		val locales = InternalConfigHandler.getLocales(project)
		for(locale in locales) {
			val element = locale.pointer.element ?: continue
			val typeFile = locale.pointer.containingFile
			val pinned = locale.id == localeIdFromFileName
			val lookupElement = LookupElementBuilder.create(element, locale.id).withIcon(locale.icon)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withPriority(if(pinned) PlsCompletionPriorities.pinnedPriority else 0.0) //优先提示与文件名匹配的语言区域
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
}
