package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*

/**
 * 提供命令字段名字的代码补全。
 */
@Suppress("UnstableApiUsage")
class ParadoxCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val project = parameters.originalFile.project
		
		//需要避免ProcessCanceledException导致完全不作任何提示
		
		runBlockingCancellable {
			doCompleteLocalisationCommand(parameters, project, result)
		}
		runBlockingCancellable {
			doCompleteScriptedLoc(project, result)
		}
		
		//TODO 补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scope.of.completions.may.be.incorrect"))
	}
	
	private fun doCompleteLocalisationCommand(parameters: CompletionParameters, project: Project, result: CompletionResultSet) {
		val gameType = parameters.originalFile.fileInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		CwtConfigHandler.completeLocalisationCommand(configGroup, result)
	}
	
	private fun doCompleteScriptedLoc(project: Project, result: CompletionResultSet) {
		//提示类型为scripted_loc的definition
		val tailText = " from scripted_loc"
		val definitions = findDefinitionsByType("scripted_loc", project, distinct = true)
		if(definitions.isEmpty()) return
		for(definition in definitions) {
			val name = definition.definitionInfo?.name.orEmpty() //不应该为空
			val icon = PlsIcons.localisationCommandFieldIcon
			val typeText = definition.containingFile.name
			val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
				.withTailText(tailText, true)
				.withTypeText(typeText, true)
			result.addElement(lookupElement)
		}
	}
}