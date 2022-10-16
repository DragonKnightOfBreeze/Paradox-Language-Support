package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptCompletionContributor : CompletionContributor() {
	init {
		val stringTokens = TokenSet.create(STRING_TOKEN, QUOTED_STRING_TOKEN)
		val propertyKeyOrStringTokens = TokenSet.create(PROPERTY_KEY_TOKEN, QUOTED_PROPERTY_KEY_TOKEN, STRING_TOKEN, QUOTED_STRING_TOKEN)
		val parameterTokens = TokenSet.create(PARAMETER_ID, INPUT_PARAMETER_ID)
		
		//当用户正在输入一个propertyKey或string时提示
		val definitionPattern = psiElement().withElementType(propertyKeyOrStringTokens)
		extend(null, definitionPattern, ParadoxDefinitionCompletionProvider())
		
		//当用户可能在输入一个eventId时提示
		val eventIdPattern = psiElement().withElementType(stringTokens)
			.withParent(psiElement(ParadoxScriptString::class.java)
				.withSuperParent(2, psiElement(ParadoxScriptProperty::class.java)
					.withParent(psiElement(ParadoxScriptBlock::class.java)
						.withSuperParent(2, psiElement(ParadoxScriptProperty::class.java)))))
		extend(null, eventIdPattern, ParadoxEventIdCompletionProvider())
		
		val parameterPattern = psiElement().withElementType(parameterTokens)
		extend(null, parameterPattern, ParadoxParameterCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}