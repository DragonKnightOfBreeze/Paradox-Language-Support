package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

class ParadoxScriptCompletionContributor : CompletionContributor() {
	class BooleanCompletionProvider : CompletionProvider<CompletionParameters>() {
		private val lookupElements = booleanValues.map{value->
			LookupElementBuilder.create(value).bold().withPriority(80.0)
		}

		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			result.addAllElements(lookupElements)
		}
	}
	
	class DefinitionPropertyNameCompletionProvider: CompletionProvider<CompletionParameters>(){
		override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
			//1. 向上得到最近的definition，从而得到definitionInfo.properties，并且算出相对的definitionPropertyPath，忽略正在填写的propertyName
			//2. 根据properties和definitionPropertyPath确定提示结果，注意当匹配子类型时才会加入对应的提示结果，否则不加入
			TODO()
		}
		
		private fun getDefinitionInfoAndDefinitionPropertyPath(){
			
		}
	}
	
	init {
		//当用户正在输入一个string时提示
		extend(CompletionType.BASIC, psiElement(STRING_TOKEN), BooleanCompletionProvider())
		extend(null, or(
			psiElement(PROPERTY_KEY_ID),
			psiElement(QUOTED_PROPERTY_KEY_ID),
			psiElement(STRING_TOKEN).withParent(ParadoxScriptBlock::class.java)
		), DefinitionPropertyNameCompletionProvider())
	}
	
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
