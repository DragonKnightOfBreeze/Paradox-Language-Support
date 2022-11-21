package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptCompletionContributor : CompletionContributor() {
	init {
		//当用户可能正在输入一个布尔值时提示
		val booleanPattern = psiElement(STRING_TOKEN)
		extend(CompletionType.BASIC, booleanPattern, ParadoxBooleanCompletionProvider())
		
		val scriptedVariableReferenceTokens = TokenSet.create(SCRIPTED_VARIABLE_REFERENCE_ID, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_ID)
		val stringTokens = TokenSet.create(STRING_TOKEN, QUOTED_STRING_TOKEN)
		val keyOrStringTokens = TokenSet.create(PROPERTY_KEY_TOKEN, QUOTED_PROPERTY_KEY_TOKEN, STRING_TOKEN, QUOTED_STRING_TOKEN)
		val parameterOrArgumentTokens = TokenSet.create(PARAMETER_ID, ARGUMENT_ID)
		
		//当用户正在输入一个scriptedVariableReference的名字时提示
		val scriptedVariableNamePattern = psiElement().withElementType(scriptedVariableReferenceTokens)
		extend(null, scriptedVariableNamePattern, ParadoxScriptedVariableCompletionProvider())
		
		//当用户正在输入一个propertyKey或string时提示
		val definitionPattern = psiElement().withElementType(keyOrStringTokens)
		extend(null, definitionPattern, ParadoxDefinitionCompletionProvider())
		
		//当用户可能在输入一个eventId时提示
		val eventIdPattern = psiElement().withElementType(stringTokens)
			.withParent(psiElement(ParadoxScriptString::class.java)
				.withParent(psiElement(ParadoxScriptProperty::class.java)
					.withParent(psiElement(ParadoxScriptBlock::class.java)
						.withParent(psiElement(ParadoxScriptProperty::class.java)))))
		extend(null, eventIdPattern, ParadoxEventIdCompletionProvider())
		
		//当用户正在输入一个parameter的名字时提示
		val parameterPattern = psiElement().withElementType(parameterOrArgumentTokens)
		extend(null, parameterPattern, ParadoxParameterCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = PlsConstants.dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
