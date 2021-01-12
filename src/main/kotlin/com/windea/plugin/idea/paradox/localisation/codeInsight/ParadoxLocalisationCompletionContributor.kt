@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.localisation.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = paradoxLocales.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationLocaleIcon)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
		}
	}
	
	class SerialNumberCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = paradoxSerialNumbers.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
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
	
	class PrimaryCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = paradoxPrimaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
		}
	}
	
	class SecondaryCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = paradoxSecondaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
		}
	}
	
	class CommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = paradoxCommandFields.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandFieldIcon)
		}
		
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val prefix = parameters.position.prevSibling?.text
			val fqResult = if(prefix != null) result.withPrefixMatcher(prefix) else result
			fqResult.addAllElements(lookupElements)
		}
	}
	
	init {
		//当用户正在输入一个locale时提示
		val localePattern = psiElement().afterSibling(psiElement(LOCALE))
		val serialNumberPattern = psiElement().afterSibling(psiElement(SERIAL_NUMBER_ID))
		val colorCodePattern = psiElement().afterSibling(psiElement(COLOR_CODE))
		val primaryCommandScopePattern = psiElement().afterSibling(psiElement(COMMAND_SCOPE))
		val secondaryCommandScopePattern = psiElement().afterSibling(psiElement(COMMAND_SCOPE))
		val commandFieldPattern = psiElement().afterSibling(psiElement(COMMAND_FIELD))
		
		extend(CompletionType.BASIC, localePattern, LocaleCompletionProvider())
		extend(CompletionType.BASIC, serialNumberPattern, SerialNumberCompletionProvider())
		extend(CompletionType.BASIC, colorCodePattern, ColorCompletionProvider())
		extend(CompletionType.BASIC, primaryCommandScopePattern, PrimaryCommandScopeCompletionProvider())
		extend(CompletionType.BASIC, secondaryCommandScopePattern, SecondaryCommandScopeCompletionProvider())
		extend(CompletionType.BASIC, commandFieldPattern, CommandFieldCompletionProvider())
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}

