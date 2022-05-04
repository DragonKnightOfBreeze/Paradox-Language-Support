package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptCompletionContributor : CompletionContributor() {
	init {
		//当用户正在输入一个string时提示
		val booleanPattern = psiElement(ParadoxScriptElementTypes.STRING_TOKEN)
		extend(CompletionType.BASIC, booleanPattern, ParadoxBooleanCompletionProvider)
		
		//当用户正在输入一个propertyKey或string时提示
		val definitionPattern = or(
			psiElement(ParadoxScriptElementTypes.PROPERTY_KEY_ID), psiElement(ParadoxScriptElementTypes.QUOTED_PROPERTY_KEY_ID),
			psiElement(ParadoxScriptElementTypes.STRING_TOKEN), psiElement(ParadoxScriptElementTypes.QUOTED_STRING_TOKEN)
		)
		extend(null, definitionPattern, ParadoxDefinitionCompletionProvider)
		
		//当用户正在输入一个eventId时提示
		val eventIdDefinition = psiElement(ParadoxScriptElementTypes.STRING_TOKEN)
			.withSuperParent(3, psiElement(ParadoxScriptProperty::class.java))
		extend(CompletionType.BASIC, eventIdDefinition, ParadoxEventIdCompletionProvider)
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}