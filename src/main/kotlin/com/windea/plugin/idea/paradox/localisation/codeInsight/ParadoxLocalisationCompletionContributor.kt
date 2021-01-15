@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.localisation.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	companion object{
		private const val dummyIdentifier = "windea"
		private const val dummyIdentifierLength = dummyIdentifier.length
		
		private val localePattern = psiElement(LOCALE_ID)
		private val serialNumberPattern = psiElement(SERIAL_NUMBER_ID)
		private val colorIdPattern = psiElement(COLOR_ID)
		private val commandScopePattern = psiElement(COMMAND_SCOPE_ID)
		private val commandFieldPattern = psiElement(COMMAND_FIELD_ID)
		
		private val localeElements = paradoxLocales.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationLocaleIcon)
		}
		private val serialNumberElements = paradoxSerialNumbers.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description)
		}
		private val colorElements = paradoxColors.map{
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(it.icon)
		}
		private val primaryCommandScopeElements = paradoxPrimaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val secondaryCommandScopeElements = paradoxSecondaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val repeatableCommandScopeElements = paradoxRepeatableCommandScopes.map{
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val commandFieldElements = paradoxCommandFields.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandFieldIcon)
		}
		
		private val eventTargetPrefixElement = LookupElementBuilder.create(eventTargetPrefix).bold().withPriority(-80.0)
	}
	
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position //LOCALE_ID
			val prefix = position.text.dropLast(dummyIdentifierLength)
			result.withPrefixMatcher(prefix).addAllElements(localeElements)
		}
	}
	
	class SerialNumberCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position //SERIAL_NUMBER_ID
			val prefix = position.text.dropLast(dummyIdentifierLength)
			result.withPrefixMatcher(prefix).addAllElements(serialNumberElements)
		}
	}
	
	class ColorCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position //COLOR_ID
			val prefix = position.text.dropLast(dummyIdentifierLength)
			result.withPrefixMatcher(prefix).addAllElements(colorElements)
		}
	}
	
	class CommandCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position //COMMAND_SCOPE_ID, COMMAND_FIELD_ID
			val prefix = position.text.dropLast(dummyIdentifierLength).trim()
			if(prefix == eventTargetPrefix) return 
			
			val parent = position.parent //COMMAND_SCOPE, COMMAND_FIELD
			if(parent !is ParadoxLocalisationCommandIdentifier) return
			
			val handledResult = result.withPrefixMatcher(prefix)
			val prev = parent.prevIdentifier as? ParadoxLocalisationCommandScope
			if(prev == null){
				handledResult.addAllElements(primaryCommandScopeElements)
				handledResult.addElement(eventTargetPrefixElement)
			}else{
				val prevScope = prev.paradoxCommandScope
				if(prevScope != null){
					val prevPrev = prev.prevIdentifier as? ParadoxLocalisationCommandScope
					if(prevPrev == null) {
						if(prevScope.isPrimary) {
							handledResult.addAllElements(secondaryCommandScopeElements)
						}
					}else{
						val prevPrevScope = prevPrev.paradoxCommandScope
						if(prevPrevScope != null){
							if(prevPrevScope.isPrimary && prevScope.isRepeatable){
								handledResult.addAllElements(repeatableCommandScopeElements)
							}
						}
					}
				}
			}
			val next = parent.nextIdentifier
			if(next == null){
				handledResult.addAllElements(commandFieldElements)
			}
		}
	}
	
	init {
		extend(CompletionType.BASIC, localePattern, LocaleCompletionProvider())
		extend(CompletionType.BASIC, serialNumberPattern, SerialNumberCompletionProvider()) //无法被匹配，但仍然留着
		extend(CompletionType.BASIC, colorIdPattern, ColorCompletionProvider()) //无法被匹配，但仍然留着
		extend(CompletionType.BASIC, commandScopePattern, CommandCompletionProvider())
		extend(CompletionType.BASIC, commandFieldPattern, CommandCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}

