package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptCompletionContributor : CompletionContributor() {
	init {
		//当用户正在输入一个string时提示
		val booleanPattern = psiElement(STRING_TOKEN)
			.withParent(ParadoxScriptString::class.java)
		extend(CompletionType.BASIC, booleanPattern, ParadoxBooleanCompletionProvider)
		
		//当用户正在输入一个propertyKey或string时提示
		val definitionPattern = psiElement()
			.withParents(ParadoxScriptPropertyKey::class.java, ParadoxScriptString::class.java)
		extend(null, definitionPattern, ParadoxDefinitionCompletionProvider)
		
		//当用户可能在输入一个eventId时提示
		val eventIdPattern = psiElement()
			.withParent(psiElement(ParadoxScriptString::class.java)
				.withSuperParent(2, psiElement(ParadoxScriptProperty::class.java)
					.withParent(psiElement(ParadoxScriptBlock::class.java)
						.withSuperParent(2, psiElement(ParadoxScriptProperty::class.java)))))
		extend(null, eventIdPattern, ParadoxEventIdCompletionProvider)
		
		//当用户可能在输入一个tag时提示
		val tagPattern = psiElement(STRING_TOKEN)
			.withParent(psiElement(ParadoxScriptString::class.java)
				.withParent((psiElement(ParadoxScriptBlock::class.java)
					.withSuperParent(2, psiElement(ParadoxScriptProperty::class.java)))))
		extend(null, tagPattern, ParadoxTagCompletionProvider)
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}