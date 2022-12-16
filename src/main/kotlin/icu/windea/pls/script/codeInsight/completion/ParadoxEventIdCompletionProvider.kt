package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.script.psi.*

/**
 * 提供事件ID中事件命名空间的代码补全。
 */
class ParadoxEventIdCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val element = parameters.position.parent?.castOrNull<ParadoxScriptString>() ?: return
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		if(keyword.contains('.')) return
		val eventIdProperty = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
		if(!eventIdProperty.name.equals("id", true)) return
		val event = eventIdProperty.parent?.parent?.castOrNull<ParadoxScriptProperty>() ?: return
		if(event.definitionInfo?.type != "event") return
		
		//仅提示脚本文件中向上查找到的那个合法的事件命名空间
		val eventNamespace = DefinitionConfigHandler.getEventNamespace(event) ?: return //skip
		val name = eventNamespace.value ?: return
		val lookupElement = LookupElementBuilder.create(eventNamespace, name)
			.withIcon(PlsIcons.EventNamespace)
			.withPriority(PlsCompletionPriorities.definitionNamePriority) //same with event ids
		result.addElement(lookupElement)
	}
}
