

package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*
import icu.windea.pls.script.codeInsight.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	companion object{
		private val localePattern = psiElement(LOCALE_ID)
		//private val sequentialNumberPattern = psiElement(SEQUENTIAL_NUMBER_ID)
		//private val colorIdPattern = psiElement(COLOR_ID)
		private val commandFieldPattern = psiElement(COMMAND_FIELD_ID)
		
		private val localeElements = getConfig().locales.map {
			LookupElementBuilder.create(it.name).withIcon(it.icon).withTailText(it.tailText,true)
		}
		//private val sequentialNumberElements = getConfig().sequentialNumbers.map {
		//	LookupElementBuilder.create(it.name).withIcon(it.icon).withTailText(it.tailText,true)
		//}
		//private val colorElements = getConfig().colors.map{
		//	LookupElementBuilder.create(it.name).withIcon(it.icon).withTailText(it.tailText,true)
		//}
	}
	
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(localeElements)
		}
	}
	
	//class SequentialNumberCompletionProvider : CompletionProvider<CompletionParameters>() {
	//	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
	//		result.addAllElements(sequentialNumberElements)
	//	}
	//}
	//
	//class ColorCompletionProvider : CompletionProvider<CompletionParameters>() {
	//	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
	//		result.addAllElements(colorElements)
	//	}
	//}
	
	class CommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position //COMMAND_FIELD_ID
			val commandField = position.parent as? ParadoxLocalisationCommandField ?: return
			val project = position.project
			val gameType = parameters.originalFile.fileInfo?.gameType?:return
			val configGroup = getConfig(project).get(gameType)?:return
			completeLocalisationCommand(commandField,configGroup, result)
			
			//TODO 补全的scope可能不正确
			result.addLookupAdvertisement(message("scopeOfCompletionsMayBeIncorrect"))
		}
	}
	
	init {
		extend(CompletionType.BASIC, localePattern, LocaleCompletionProvider())
		//extend(CompletionType.BASIC, sequentialNumberPattern, SequentialNumberCompletionProvider()) //NOTE 无法被匹配
		//extend(CompletionType.BASIC, colorIdPattern, ColorCompletionProvider()) //NOTE 无法被匹配
		extend(null, commandFieldPattern, CommandFieldCompletionProvider()) //TODO 匹配scope
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}

