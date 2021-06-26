package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptCompletionContributor : CompletionContributor() {
	companion object {
		private val stringPattern = psiElement(STRING_TOKEN)
		private val definitionPattern = or(
			psiElement(PROPERTY_KEY_ID), psiElement(QUOTED_PROPERTY_KEY_ID),
			psiElement(STRING_TOKEN), psiElement(QUOTED_STRING_TOKEN)
		)
		
		private val booleanLookupElements = booleanValues.map { value ->
			LookupElementBuilder.create(value).bold().withPriority(keywordPriority)
		}
	}
	
	class BooleanCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(booleanLookupElements) //总是提示
		}
	}
	
	class DefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			val position = parameters.position
			val parent1 = position.parent
			val parent2 = parent1.parent
			val mayBeKey = parent1 is ParadoxScriptPropertyKey || parent2 is ParadoxScriptBlock
			val mayBeValue = parent2 is ParadoxScriptPropertyValue
			val mayBeValueInBlock = parent2 is ParadoxScriptBlock
			
			ProgressManager.checkCanceled()
			
			if(mayBeKey) {
				//得到key元素
				val keyElement = parent1
				//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
				val definitionProperty = keyElement.findParentDefinitionPropertySkipThis() ?: return
				//进行提示
				addKeyCompletions(keyElement, definitionProperty, result)
			}
			if(mayBeValue) {
				//得到value元素
				val valueElement = parent1
				//得到上一级definitionProperty
				val definitionProperty = valueElement.findParentDefinitionProperty() ?: return
				//进行提示
				addValueCompletions(valueElement, definitionProperty, result)
			}
			if(mayBeValueInBlock) {
				//得到value元素
				val valueElement = parent1
				//得到上一级definitionProperty
				val definitionProperty = valueElement.findParentDefinitionProperty() ?: return
				//进行提示
				addValueCompletionsInBlock(valueElement, definitionProperty, result)
			}
		}
	}
	
	init {
		//当用户正在输入一个string时提示
		extend(CompletionType.BASIC, stringPattern, BooleanCompletionProvider())
		//当用户正在输入一个propertyKey或string时提示
		extend(null, definitionPattern, DefinitionCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = dummyIdentifier
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
