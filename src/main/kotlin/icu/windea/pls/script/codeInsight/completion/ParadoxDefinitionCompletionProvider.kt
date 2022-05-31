package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*

/**
 * 提示定义的相关代码补全。基于CWT规则文件。
 */
object ParadoxDefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val propertyKeyOrStringElement = parameters.position.parent
		val blockOrPropertyValueElement = propertyKeyOrStringElement.parentOfTypes(ParadoxScriptPropertyValue::class, ParadoxScriptBlock::class)
		val mayBeKey = propertyKeyOrStringElement is ParadoxScriptPropertyKey || blockOrPropertyValueElement is ParadoxScriptBlock
		val mayBeValue = propertyKeyOrStringElement is ParadoxScriptString && blockOrPropertyValueElement is ParadoxScriptPropertyValue
		val mayBeValueInBlock = propertyKeyOrStringElement is ParadoxScriptString && blockOrPropertyValueElement is ParadoxScriptBlock
		
		ProgressManager.checkCanceled()
		
		if(mayBeKey) {
			//得到key元素
			val keyElement = propertyKeyOrStringElement
			//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
			val definitionProperty = keyElement.findParentDefinitionProperty(fromParentBlock = true) ?: return
			//进行提示
			addKeyCompletions(keyElement, definitionProperty, result)
		}
		if(mayBeValue) {
			//得到value元素
			val valueElement = propertyKeyOrStringElement
			//得到上一级definitionProperty
			val definitionProperty = valueElement.findParentDefinitionProperty() ?: return
			//进行提示
			addValueCompletions(valueElement, definitionProperty, result)
		}
		if(mayBeValueInBlock) {
			//得到value元素
			val valueElement = propertyKeyOrStringElement
			//得到上一级definitionProperty
			val definitionProperty = valueElement.findParentDefinitionProperty() ?: return
			//进行提示
			addValueCompletionsInBlock(valueElement, definitionProperty, result)
		}
		
		//TODO 补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scope.of.completions.may.be.incorrect"))
	}
}
