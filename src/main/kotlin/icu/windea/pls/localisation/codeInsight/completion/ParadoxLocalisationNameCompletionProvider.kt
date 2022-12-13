package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供本地化名字的代码补全。
 * 主要是自动插入后面的冒号、数字以及双引号，并将光标放到合适的位置。
 */
class ParadoxLocalisationNameCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		ProgressManager.checkCanceled()
		val offsetInParent = parameters.offset - parameters.position.textRange.startOffset
		val keyword = parameters.position.getKeyword(offsetInParent)
		if(keyword.isEmpty()) return
		val propertyKeyElement = parameters.position.parent as? ParadoxLocalisationPropertyKey ?: return
		if(propertyKeyElement.nextSibling != null) return //后面没有冒号、双引号等其他内容
		val file = parameters.originalFile as? ParadoxLocalisationFile ?: return
		val localisationCategory = ParadoxLocalisationCategory.resolve(file) ?: return
		val textToInsert = when(localisationCategory) {
			ParadoxLocalisationCategory.Localisation -> ":0 \"\""
			ParadoxLocalisationCategory.SyncedLocalisation -> ": \"\""
		}
		val lookupElement = LookupElementBuilder.create(textToInsert)
			.withPresentableText(keyword)
			.bold()
			.withTailText(textToInsert)
			.withIcon(PlsIcons.Localisation)
			.withInsertHandler { c, _ ->
				//把鼠标放到引号中间
				val caretModel = c.editor.caretModel
				caretModel.moveToOffset(caretModel.offset - 1)
			}
		result.withPrefixMatcher("").addElement(lookupElement)
	}
}
