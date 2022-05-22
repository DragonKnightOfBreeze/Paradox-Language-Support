package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.project.Project
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供内置的命令字段的代码补全（非scripted_loc）。基于CWT规则文件。
 */
object ParadoxCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val position = parameters.position //COMMAND_FIELD_ID
		if(position.elementType != ParadoxLocalisationElementTypes.COMMAND_FIELD_ID) return
		val project = position.project
		val gameType = parameters.originalFile.fileInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		completeLocalisationCommand(configGroup, result)
		
		doCompleteScriptedLoc(project, result)
		
		//TODO 补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scopeOfCompletionsMayBeIncorrect"))
	}
	
	private fun doCompleteScriptedLoc(project: Project, result: CompletionResultSet){
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