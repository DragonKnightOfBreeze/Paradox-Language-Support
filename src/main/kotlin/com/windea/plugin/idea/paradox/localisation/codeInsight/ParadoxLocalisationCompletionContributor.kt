@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.localisation.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	companion object{
		private const val dummyIdentifier = "windea"
		private const val dummyIdentifierLength = dummyIdentifier.length
		
		private val localeLookupElements = paradoxLocales.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationLocaleIcon)
		}
		private val serialNumberLookupElements = paradoxSerialNumbers.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description)
		}
		private val primaryCommandScopeLookupElements = paradoxPrimaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val secondaryCommandScopeLookupElements = paradoxSecondaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val commandFieldLookupElements = paradoxCommandFields.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandFieldIcon)
		}
	}
	
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.text.dropLast(dummyIdentifierLength)
			val fqResult = result.withPrefixMatcher(prefix)
			fqResult.addAllElements(localeLookupElements)
		}
	}
	
	class SerialNumberCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(serialNumberLookupElements)
		}
	}
	
	class ColorCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = paradoxColors.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(it.icon)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
		}
	}
	
	class CommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.text.dropLast(dummyIdentifierLength)
			val fqResult = result.withPrefixMatcher(prefix)
			fqResult.addAllElements(primaryCommandScopeLookupElements)
			fqResult.addAllElements(secondaryCommandScopeLookupElements)
		}
	}
	
	class CommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.text.dropLast(dummyIdentifierLength)
			val fqResult = result.withPrefixMatcher(prefix)
			fqResult.addAllElements(secondaryCommandScopeLookupElements)
			fqResult.addAllElements(commandFieldLookupElements)
		}
	}
	
	init {
		//当用户正在输入一个locale时提示
		val localePattern = psiElement(LOCALE_ID)
		val serialNumberPattern = psiElement(SERIAL_NUMBER_ID)
		val colorCodePattern = psiElement(COLOR_CODE)
		val commandScopePattern = psiElement(COMMAND_SCOPE_TOKEN)
		val commandFieldPattern = psiElement(COMMAND_FIELD_TOKEN)
		
		extend(CompletionType.BASIC, localePattern, LocaleCompletionProvider())
		extend(CompletionType.BASIC, serialNumberPattern, SerialNumberCompletionProvider())
		extend(CompletionType.BASIC, colorCodePattern, ColorCompletionProvider())
		extend(CompletionType.BASIC, commandScopePattern, CommandScopeCompletionProvider())
		extend(CompletionType.BASIC, commandFieldPattern, CommandFieldCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}

