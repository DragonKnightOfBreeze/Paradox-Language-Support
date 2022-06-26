package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.script.psi.*

/**
 * 提供事件ID的前缀的代码补全。基于对应的事件命名空间。
 */
class ParadoxEventIdCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//val file = parameters.originalFile
		//if(file !is ParadoxScriptFile) return
		//val fileInfo = file.fileInfo ?: return
		//if(!"events".matchesPath(fileInfo.path.path, acceptSelf = false)) return
		//val rootBlock = file.block ?: return
		//val properties = rootBlock.propertyList
		//if(properties.isEmpty()) return //空文件，跳过
		//if(properties.first { it.name.equals("namespace", true) } == null) return //没有事件命名空间，跳过
		val stringElement = parameters.position.parent.castOrNull<ParadoxScriptString>() ?: return
		val eventIdProperty = stringElement.parentOfType<ParadoxScriptProperty>() ?: return
		if(!eventIdProperty.name.equals("id", true)) return
		val eventDefinition = eventIdProperty.parentOfType<ParadoxScriptProperty>() ?: return
		if(eventDefinition.definitionInfo?.type != "event") return
		val namespace = DefinitionConfigHandler.getEventNamespace(eventDefinition) ?: return //找不到，跳过
		val lookupElement = LookupElementBuilder.create("$namespace.")
		result.addElement(lookupElement)
	}
}