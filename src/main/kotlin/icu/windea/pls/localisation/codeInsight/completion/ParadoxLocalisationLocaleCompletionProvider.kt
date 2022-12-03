package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供语言区域名字的代码补全。
 */
class ParadoxLocalisationLocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
	private fun getInsertHandler(insertColon: Boolean): InsertHandler<LookupElement> {
		return InsertHandler { context, _ ->
			//如果之后没有英文冒号，则插入：英文冒号+换行符+缩进，否则光标移到冒号之后
			val editor = context.editor
			if(insertColon) {
				val settings = CodeStyle.getSettings(context.file)
				val indentOptions = settings.getIndentOptions(ParadoxLocalisationFileType)
				val s = ":\n" + " ".repeat(indentOptions.INDENT_SIZE)
				EditorModificationUtil.insertStringAtCaret(editor, s)
			} else {
				val chars = editor.document.charsSequence
				val colonIndex = chars.indexOf(':', context.startOffset)
				if(colonIndex != -1) {
					editor.caretModel.moveToOffset(colonIndex + 1)
				}
			}
		}
	}
	
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//直到所在行开始没有任何空白，直到所在行结束没有除了冒号之外的任何其他字符
		val position = parameters.position
		if(position.elementType == ParadoxLocalisationElementTypes.PROPERTY_KEY_ID) {
			if(position.nextSibling != null || position.parent?.parent?.prevSibling?.let {
					it.elementType != TokenType.WHITE_SPACE || it.text.last().let { c -> c != '\n' && c != '\r' }
				} == true) return
		}
		val insertColon = position.nextSibling.elementType != ParadoxLocalisationElementTypes.COLON
		val file = parameters.originalFile
		val project = file.project
		val localeIdFromFileName = file.castOrNull<ParadoxLocalisationFile>()?.getLocaleIdFromFileName()
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		val locales = getCwtConfig(project).core.localisationLocales.values
		val insertHandler = getInsertHandler(insertColon)
		for(locale in locales) {
			val element = locale.pointer.element ?: continue
			val typeFile = locale.pointer.containingFile
			val matched = localeIdFromFileName?.let { it == locale.id }
			val lookupElement = LookupElementBuilder.create(element, locale.id).withIcon(locale.icon)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withInsertHandler(insertHandler)
				.letIf(matched == false) {
					it.withItemTextForeground(JBColor.GRAY) //将不匹配的语言区域的提示项置灰
				}
				.letIf(matched == true) {
					it.withPriority(PlsCompletionPriorities.pinnedPriority) //优先提示与文件名匹配的语言区域
				}
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
}
