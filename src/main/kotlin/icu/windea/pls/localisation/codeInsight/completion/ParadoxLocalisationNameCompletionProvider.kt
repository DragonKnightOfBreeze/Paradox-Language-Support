package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiErrorElement
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
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
		if(keyword.length != offsetInParent) return
		val propertyKey = parameters.position.parent as? ParadoxLocalisationPropertyKey ?: return
		if(!shouldComplete(propertyKey)) return //后面没有冒号、双引号等其他内容
		val file = parameters.originalFile as? ParadoxLocalisationFile ?: return
		val localisationCategory = ParadoxLocalisationCategory.resolve(file) ?: return
		val textToInsert = when(localisationCategory) {
			ParadoxLocalisationCategory.Localisation -> ":0 \"\""
			ParadoxLocalisationCategory.SyncedLocalisation -> ": \"\""
		}
		val lookupElement = LookupElementBuilder.create("")
			.withPresentableText("<key>")
			.bold()
			.withTailText(textToInsert)
			.withIcon(PlsIcons.Localisation)
			.withInsertHandler { c, _ ->
				val editor = c.editor
				//插入后续文本
				EditorModificationUtil.insertStringAtCaret(editor, textToInsert)
				//把鼠标放到引号中间
				val caretModel = editor.caretModel
				caretModel.moveToOffset(caretModel.offset - 1)
			}
		result.withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE).addElement(lookupElement)
	}
	
	private fun shouldComplete(propertyKey: ParadoxLocalisationPropertyKey): Boolean {
		val nextSibling = propertyKey.nextSibling
		return nextSibling is PsiErrorElement && nextSibling.textLength == 0
	}
}
