package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import org.jetbrains.kotlin.idea.completion.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	init {
		//当用户可能正在输入一个locale的名字时提示
		val localePattern = psiElement(LOCALE_ID) or psiElement(PROPERTY_KEY_ID)
		extend(CompletionType.BASIC, localePattern, ParadoxLocalisationLocaleCompletionProvider())
		
		//当用户正在输入一个propertyReference的名字时提示
		val propertyReferencePattern = psiElement(PROPERTY_REFERENCE_ID)
		extend(null, propertyReferencePattern, ParadoxLocalisationPropertyReferenceCompletionProvider())
		
		//当用户可能正在输入一个icon的名字时提示
		val iconPattern = psiElement(ICON_ID)
		extend(null, iconPattern, ParadoxLocalisationIconCompletionProvider())
		
		//当用户可能可能正在输入一个color的ID时提示（因为colorId只有一个字符，这里需要特殊处理）
		val colorPattern = psiElement().afterLeaf(psiElement().withText("§"))
		extend(null, colorPattern, ParadoxLocalisationColorCompletionProvider())
		
		//当用户正在输入一个commandScope的名字时提示
		val commandScopePattern = psiElement(COMMAND_SCOPE_ID) or psiElement(COMMAND_FIELD_ID)
		extend(null, commandScopePattern, ParadoxLocalisationCommandScopeCompletionProvider())
		
		//当用户正在输入一个commandField的名字时提示
		val commandFieldPattern = psiElement(COMMAND_FIELD_ID)
		extend(null, commandFieldPattern, ParadoxLocalisationCommandFieldCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = PlsConstants.dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
