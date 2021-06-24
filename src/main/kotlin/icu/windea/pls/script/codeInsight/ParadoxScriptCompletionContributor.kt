package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*

@Suppress("UNCHECKED_CAST")
class ParadoxScriptCompletionContributor : CompletionContributor() {
	companion object {
		private val stringPattern = psiElement(STRING_TOKEN)
		
		//不对引号括起的propertyKey或string进行提示
		private val definitionPattern = or(psiElement(PROPERTY_KEY_ID), psiElement(STRING_TOKEN))
		//private val propertyNamePattern = and(
		//	psiElement().withParent(ParadoxScriptBlock::class.java),
		//	or(psiElement(PROPERTY_KEY_ID), psiElement(QUOTED_PROPERTY_KEY_ID), psiElement(STRING_TOKEN), psiElement(QUOTED_STRING_TOKEN))
		//)
		//private val propertyValuePattern = or(psiElement(STRING_TOKEN), psiElement(QUOTED_STRING_TOKEN))
		
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
			val parent1 = position.parent ?: return //propertyKey | value
			val parent2 = parent1.parent ?: return //property | propertyValue | block
			val parent3 = parent2.parent ?: return //block | propertyValue
			val mayBeKey = parent1 is ParadoxScriptPropertyKey || parent3 is ParadoxScriptPropertyValue
			val mayBeValue = parent1 is ParadoxScriptValue
			
			ProgressManager.checkCanceled()
			
			when {
				mayBeKey && mayBeValue -> {
					//得到key或value元素
					val keyOrValue = parent1
					//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
					val definitionProperty = keyOrValue.findParentDefinitionProperty(true) ?: return
					addKeyCompletions(keyOrValue,definitionProperty,result)
					addValueCompletions(keyOrValue,definitionProperty,result)
				}
				mayBeKey -> {
					//得到key元素
					val key = parent1
					//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
					val definitionProperty = key.findParentDefinitionProperty(true) ?: return
					addKeyCompletions(key,definitionProperty,result)
				}
				mayBeValue -> {
					//得到key元素
					val value = parent1
					//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
					val definitionProperty = value.findParentDefinitionProperty(true) ?: return
					addValueCompletions(value,definitionProperty,result)
				}
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
