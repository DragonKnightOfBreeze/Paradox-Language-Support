

package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*

@Suppress("HasPlatformType")
class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	companion object{
		private const val dummyIdentifier = "windea"
		private const val dummyIdentifierLength = dummyIdentifier.length
		
		private val localePattern = psiElement(LOCALE_ID)
		private val sequentialNumberPattern = psiElement(SEQUENTIAL_NUMBER_ID)
		private val colorIdPattern = psiElement(COLOR_ID)
		//private val commandScopePattern = psiElement(COMMAND_SCOPE_ID)
		//private val commandFieldPattern = psiElement(COMMAND_FIELD_ID)
		
		private val localeElements = getConfig().locales.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationLocaleIcon)
		}
		private val sequentialNumberElements = getConfig().sequentialNumbers.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationSequentialNumberIcon)
		}
		private val colorElements = getConfig().colors.map{
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(it.icon)
		}
		//private val primaryCommandScopeElements = config.primaryCommandScopes.map {
		//	LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		//}
		//private val secondaryCommandScopeElements = config.secondaryCommandScopes.map {
		//	LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		//}
		//private val commandFieldElements = config.commandFields.map {
		//	LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandFieldIcon)
		//}
	}
	
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(localeElements)
		}
	}
	
	class SequentialNumberCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(sequentialNumberElements)
		}
	}
	
	class ColorCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(colorElements)
		}
	}
	
	//class CommandCompletionProvider : CompletionProvider<CompletionParameters>() {
	//	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
	//		val position  = parameters.position
	//		val prefix = position.text.dropLast(dummyIdentifierLength).trim()
	//		if(prefix == eventTargetPrefix) return 
	//		
	//		val parent = position.parent //COMMAND_SCOPE, COMMAND_FIELD
	//		if(parent !is ParadoxLocalisationCommandIdentifier) return
	//		
	//		val prev = parent.prevIdentifier
	//		if(prev == null){
	//			//primaryCommandScope, secondaryCommandScope, event_target
	//			result.addAllElements(primaryCommandScopeElements)
	//			result.addAllElements(secondaryCommandScopeElements)
	//		}else{
	//			//secondaryCommandScope
	//			result.addAllElements(secondaryCommandScopeElements)
	//		}
	//		val next = parent.nextIdentifier
	//		if(next == null){
	//			//commandField, scopeVariable, scriptedLoc
	//			result.addAllElements(commandFieldElements)
	//		}
	//	}
	//}
	
	init {
		extend(CompletionType.BASIC, localePattern, LocaleCompletionProvider())
		extend(CompletionType.BASIC, sequentialNumberPattern, SequentialNumberCompletionProvider()) //无法被匹配，但仍然留着
		extend(CompletionType.BASIC, colorIdPattern, ColorCompletionProvider()) //无法被匹配，但仍然留着
		//extend(CompletionType.BASIC, commandScopePattern, CommandCompletionProvider())
		//extend(CompletionType.BASIC, commandFieldPattern, CommandCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}

