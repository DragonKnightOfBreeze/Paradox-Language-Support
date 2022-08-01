package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.util.selector.*

/**
 * 提供命令字段名字的代码补全。
 */
@Suppress("UnstableApiUsage")
class ParadoxCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val originalFile = parameters.originalFile
		val project = originalFile.project
		
		//需要避免ProcessCanceledException导致完全不作任何提示
		
		runBlockingCancellable {
			doCompleteLocalisationCommand(parameters, project, result)
		}
		runBlockingCancellable {
			doCompleteScriptedLoc(originalFile, project, result)
		}
		
		//TODO 补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scope.of.completions.may.be.incorrect"))
	}
	
	private fun doCompleteLocalisationCommand(parameters: CompletionParameters, project: Project, result: CompletionResultSet) {
		val gameType = parameters.originalFile.fileInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		CwtConfigHandler.completeLocalisationCommand(configGroup, result)
	}
	
	private fun doCompleteScriptedLoc(file: PsiFile, project: Project, result: CompletionResultSet) {
		//提示类型为scripted_loc的definition
		val tailText = " from scripted_loc"
		val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
		val definitions = findAllDefinitionsByType("scripted_loc", project, distinct = true, selector = selector)
		if(definitions.isEmpty()) return
		for(definition in definitions) {
			val name = definition.definitionInfo?.name.orEmpty() //不应该为空
			val icon = PlsIcons.LocalisationCommandField
			val typeText = definition.containingFile.name
			val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
				.withTailText(tailText, true)
				.withTypeText(typeText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
		}
	}
}