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
		private val sequentialNumberPattern = psiElement(SEQUENTIAL_NUMBER_ID)
		private val colorIdPattern = psiElement(COLOR_ID)
		private val commandScopePattern = psiElement(COMMAND_SCOPE_ID)
		private val commandFieldPattern = psiElement(COMMAND_FIELD_ID)
		
		private val localeElements = paradoxLocales.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationLocaleIcon)
		}
		private val sequentialNumberElements = paradoxSequentialNumbers.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationSequentialNumberIcon)
		}
		private val primaryCommandScopeElements = paradoxPrimaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val secondaryCommandScopeElements = paradoxSecondaryCommandScopes.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandScopeIcon)
		}
		private val commandFieldElements = paradoxCommandFields.map {
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(localisationCommandFieldIcon)
		}
		private val colorElements = paradoxColors.map{
			LookupElementBuilder.create(it.name).withTypeText(it.description).withIcon(it.icon)
		}
	}
	
	class LocaleCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position //LOCALE_ID
			val prefix = position.text.dropLast(dummyIdentifierLength)
			result.withPrefixMatcher(prefix).addAllElements(localeElements)
		}
	}
	
	class SequentialNumberCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(sequentialNumberElements)
		}
	}
	
	class CommandCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position  = parameters.position
			val prefix = position.text.dropLast(dummyIdentifierLength).trim()
			if(prefix == eventTargetPrefix) return 
			
			val parent = position.parent //COMMAND_SCOPE, COMMAND_FIELD
			if(parent !is ParadoxLocalisationCommandIdentifier) return
			
			val prev = parent.prevIdentifier
			if(prev == null){
				//primaryCommandScope, secondaryCommandScope, event_target
				result.addAllElements(primaryCommandScopeElements)
				result.addAllElements(secondaryCommandScopeElements)
			}else{
				//secondaryCommandScope
				result.addAllElements(secondaryCommandScopeElements)
			}
			val next = parent.nextIdentifier
			if(next == null){
				//commandField, scopeVariable, scriptedLoc
				result.addAllElements(commandFieldElements)
			}
		}
	}
	
	class ColorCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(colorElements)
		}
	}
	
	init {
		extend(CompletionType.BASIC, localePattern, LocaleCompletionProvider())
		extend(CompletionType.BASIC, sequentialNumberPattern, SequentialNumberCompletionProvider()) //无法被匹配，但仍然留着
		extend(CompletionType.BASIC, commandScopePattern, CommandCompletionProvider())
		extend(CompletionType.BASIC, commandFieldPattern, CommandCompletionProvider())
		extend(CompletionType.BASIC, colorIdPattern, ColorCompletionProvider()) //无法被匹配，但仍然留着
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}

