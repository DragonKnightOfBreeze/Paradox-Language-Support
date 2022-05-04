package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
	init {
		//当用户正在输入一个locale（也有可能是propertyKey）时提示
		val localePattern = or(
			psiElement(ParadoxLocalisationElementTypes.LOCALE_ID),
			psiElement(ParadoxLocalisationElementTypes.PROPERTY_KEY_ID)
		)
		extend(CompletionType.BASIC, localePattern, ParadoxLocaleCompletionProvider)
		
		//当用户正在输入一个commandField时提示
		val commandFieldPattern = psiElement(ParadoxLocalisationElementTypes.COMMAND_FIELD_ID)
		extend(null, commandFieldPattern, ParadoxCommandFieldCompletionProvider)
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}