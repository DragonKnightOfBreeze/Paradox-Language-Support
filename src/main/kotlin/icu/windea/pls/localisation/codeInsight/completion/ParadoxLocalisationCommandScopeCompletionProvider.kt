package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeLocalisationCommandScope
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供命令字段作用域的代码补全。
 */
class ParadoxLocalisationCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val offsetInParent = parameters.offset - parameters.position.textRange.startOffset
		val keyword = parameters.position.getKeyword(offsetInParent)
		val file = parameters.originalFile
		val project = file.project
		val gameType = file.fileInfo?.rootInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		
		val prevScope = parameters.position.parentOfType<ParadoxLocalisationCommandIdentifier>()?.prevIdentifier?.name
		if(prevScope != null) context.put(PlsCompletionKeys.prevScopeKey, prevScope)
		
		//提示scope
		context.completeLocalisationCommandScope(configGroup, result)
		
		//提示value[event_target]
		ProgressManager.checkCanceled()
		val eventTargetSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val eventTargetQuery = ParadoxValueSetValueSearch.search("event_target", project, selector = eventTargetSelector)
		eventTargetQuery.processResult { eventTarget ->
			val value = eventTarget.value.substringBefore('@')
			val icon = PlsIcons.ValueSetValue
			val tailText = " from value[event_target]"
			val lookupElement = LookupElementBuilder.create(eventTarget, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
		
		//提示value[global_event_target]
		ProgressManager.checkCanceled()
		val globalEventTargetSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val globalEventTargetQuery = ParadoxValueSetValueSearch.search("global_event_target", project, selector = globalEventTargetSelector)
		globalEventTargetQuery.processResult { globalEventTarget ->
			val value = globalEventTarget.value.substringBefore('@')
			val icon = PlsIcons.ValueSetValue
			val tailText = " from value[global_event_target]"
			val lookupElement = LookupElementBuilder.create(globalEventTarget, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
	}
}
