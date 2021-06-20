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
			val mayBePropertyKey = parent1 is ParadoxScriptPropertyKey || parent3 is ParadoxScriptPropertyValue
			val mayBePropertyValue = parent2 is ParadoxScriptPropertyValue
			val mayBeValue = parent1 is ParadoxScriptValue
			ProgressManager.checkCanceled()
			
			//如果可能是propertyKey，则要提示可能的propertyKey
			if(mayBePropertyKey) {
				//得到key元素（不一定是scriptPropertyKey）
				val key = parent1	
				//得到上一级block
				val block = (if(parent1 is ParadoxScriptPropertyKey) parent3 else parent2) as ParadoxScriptBlock
				//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
				val definitionProperty = block.findParentDefinitionProperty() ?: return
				addKeyCompletions(key,definitionProperty,result)
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
