package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	init {
		//当用户可能正在输入一个locale的名字时提示
		val localePattern = or(psiElement(LOCALE_ID), psiElement(PROPERTY_KEY_TOKEN))
		extend(null, localePattern, ParadoxLocalisationLocaleCompletionProvider())
		
		//当用户可能正在输入一个propertyReference的名字时提示
		val propertyReferencePattern = psiElement(PROPERTY_REFERENCE_ID)
		extend(null, propertyReferencePattern, ParadoxLocalisationPropertyReferenceCompletionProvider())
		
		//当用户可能正在输入一个icon的名字时提示
		val iconPattern = psiElement(ICON_ID)
		extend(null, iconPattern, ParadoxLocalisationIconCompletionProvider())
		
		//当用户可能正在输入一个color的ID时提示（因为colorId只有一个字符，这里需要特殊处理）
		val colorPattern = psiElement().atStartOf(psiElement().afterLeaf("§")) 
		extend(null, colorPattern, ParadoxLocalisationColorCompletionProvider())
		
		//当用户可能正在输入一个commandScope的名字时提示
		val commandScopePattern = psiElement(COMMAND_SCOPE_ID)
		extend(null, commandScopePattern, ParadoxLocalisationCommandScopeCompletionProvider())
		
		//当用户可能正在输入一个commandScope或者commandField的名字时提示
		val commandFieldPattern = psiElement(COMMAND_FIELD_ID)
		extend(null, commandFieldPattern, ParadoxLocalisationCommandFieldCompletionProvider())
		
		//当用户可能正在输入一个scriptedVariableReference的名字时提示
		val scriptedVariableNamePattern = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_ID)
		extend(null, scriptedVariableNamePattern, ParadoxScriptedVariableCompletionProvider())
		
		//当用户可能正在输入一个stellarisNamePart的名字时提示
		val stellarisNamePartPattern = psiElement().withElementType(STELLARIS_NAME_FORMAT_ID)
		extend(null, stellarisNamePartPattern, ParadoxStellarisNamePartCompletionProvider())
		
		//当用户可能正在输入一个localisation的名字时提示
		val propertyKeyPattern = psiElement(PROPERTY_KEY_TOKEN)
		extend(null, propertyKeyPattern, ParadoxLocalisationNameCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = PlsConstants.dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
