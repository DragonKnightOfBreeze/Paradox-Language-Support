package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供命令字段名字的代码补全。
 */
class ParadoxLocalisationCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val element = parameters.position.parent.castOrNull<ParadoxLocalisationCommandIdentifier>() ?: return
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		val file = parameters.originalFile
		val project = file.project
		val gameType = file.fileInfo?.rootInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.contextElementKey, element)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		context.put(PlsCompletionKeys.scopeContextKey, ParadoxScopeConfigHandler.getScopeContext(element, file))
		
		//提示scope
		CwtConfigHandler.completeSystemScope(context, result)
		CwtConfigHandler.completeLocalisationCommandScope(context, result)
		
		//提示command
		CwtConfigHandler.completeLocalisationCommandField(context, result)
		
		ProgressManager.checkCanceled()
		//提示<scripted_loc>
		CwtConfigHandler.completeScriptedLoc(file, result)
		
		ProgressManager.checkCanceled()
		//提示value[event_target]和value[global_event_target]
		CwtConfigHandler.completeEventTarget(file, result)
		
		ProgressManager.checkCanceled()
		//提示value[variable]
		CwtConfigHandler.completeVariable(file, result)
	}
}

