package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.core.*
import icu.windea.pls.core.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供颜色ID的代码补全。
 */
class ParadoxLocalisationColorCompletionProvider : CompletionProvider<CompletionParameters>() {
	private val insertHandler = InsertHandler<LookupElement> { context, _ ->
		val editor = context.editor
		val offset = editor.caretModel.offset
		editor.document.deleteString(offset, offset + 1)
	}
	
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val file = parameters.originalFile
		val originalColorId = file.findElementAt(parameters.offset)
			?.takeIf { it.elementType == ParadoxLocalisationElementTypes.COLOR_ID }
		val project = file.project
		val gameType = selectGameType(file) ?: return
		val colorConfigs = ParadoxTextColorHandler.getTextColorInfos(gameType, project, file)
		val lookupElements = mutableListOf<LookupElement>()
		for(colorConfig in colorConfigs) {
			val element = colorConfig.pointer.element ?: continue
			val name = colorConfig.name
			val icon = colorConfig.icon
			val tailText = " from <textcolor>"
			val typeFile = colorConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.letIf(originalColorId != null) {
					//delete existing colorId
					it.withInsertHandler(insertHandler)
				}
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
}
