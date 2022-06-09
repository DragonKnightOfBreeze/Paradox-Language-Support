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
class ParadoxDefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val propertyKeyOrStringElement = parameters.position.parent
		val blockOrPropertyValueElement = propertyKeyOrStringElement.parentOfTypes(ParadoxScriptPropertyValue::class, ParadoxScriptBlock::class)
		val mayBeKey = propertyKeyOrStringElement is ParadoxScriptPropertyKey || blockOrPropertyValueElement is ParadoxScriptBlock
		val mayBeValue = propertyKeyOrStringElement is ParadoxScriptString && blockOrPropertyValueElement is ParadoxScriptPropertyValue
		val mayBeValueInBlock = propertyKeyOrStringElement is ParadoxScriptString && blockOrPropertyValueElement is ParadoxScriptBlock
		
		ProgressManager.checkCanceled()
		
		//目前版本的补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scope.of.completions.may.be.incorrect"))
		
		var continueComplete: Boolean
		if(mayBeKey) {
			//得到key元素
			val keyElement = propertyKeyOrStringElement
			//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
			val definitionProperty = keyElement.findParentDefinitionProperty(fromParentBlock = true) ?: return
			//进行提示
			continueComplete = CwtConfigHandler.addKeyCompletions(keyElement, definitionProperty, result)
			if(!continueComplete) return
		}
		if(mayBeValue) {
			//得到value元素
			val valueElement = propertyKeyOrStringElement
			//得到上一级definitionProperty
			val definitionProperty = valueElement.findParentDefinitionProperty() ?: return
			//进行提示
			continueComplete = CwtConfigHandler.addValueCompletions(valueElement, definitionProperty, result)
			if(!continueComplete) return
		}
		if(mayBeValueInBlock) {
			//得到value元素
			val valueElement = propertyKeyOrStringElement
			//得到上一级block
			val blockElement = blockOrPropertyValueElement as ParadoxScriptBlock
			//进行提示
			continueComplete = CwtConfigHandler.addValueCompletionsInBlock(valueElement, blockElement, result)
			if(!continueComplete) return
		}
	}
}
