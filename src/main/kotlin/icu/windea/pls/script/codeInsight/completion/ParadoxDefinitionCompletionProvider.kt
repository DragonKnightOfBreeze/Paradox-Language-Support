package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*

/**
 * 提示定义的相关代码补全。基于CWT规则文件。
 */
object ParadoxDefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
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
		
		//TODO 补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scopeOfCompletionsMayBeIncorrect"))
	}
}
