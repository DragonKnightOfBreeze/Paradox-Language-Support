package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.script.psi.*

/**
 * 提示定义的相关代码补全。基于CWT规则文件。
 */
class ParadoxDefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val position = parameters.position
		val element = position.parent ?: return
		
		ProgressManager.checkCanceled()
		
		val quoted = element.text.isLeftQuoted()
		val rightQuoted = element.text.isRightQuoted()
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.contextElementKey, element)
		context.put(PlsCompletionKeys.originalFileKey, parameters.originalFile)
		context.put(PlsCompletionKeys.quotedKey, quoted)
		context.put(PlsCompletionKeys.rightQuotedKey, rightQuoted)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		
		val resultToUse = result.withPrefixMatcher(keyword)
		
		val mayBePropertyKey = element is ParadoxScriptPropertyKey || (element is ParadoxScriptValue && element.isBlockValue())
		val mayBePropertyValue = element is ParadoxScriptString && element.isPropertyValue()
		val mayBeBlockValue = element is ParadoxScriptString && element.isBlockValue()
		
		if(mayBePropertyKey) {
			//得到key元素（其类型也可能是ParadoxScriptString）
			val keyElement = element
			//得到上一级definitionProperty（跳过可能正在填写的definitionProperty）
			val definitionProperty = keyElement.findParentDefinitionProperty(fromParentBlock = true) ?: return
			//进行提示
			CwtConfigHandler.addKeyCompletions(keyElement, definitionProperty, context, resultToUse)
		}
		if(mayBePropertyValue) {
			//得到value元素
			val valueElement = element
			//得到上一级definitionProperty
			val definitionProperty = valueElement.findParentDefinitionProperty() ?: return
			//进行提示
			CwtConfigHandler.addValueCompletions(valueElement, definitionProperty, context, resultToUse)
		}
		if(mayBeBlockValue) {
			//得到value元素
			val valueElement = element
			//得到上一级block
			val blockElement = element.parent as? ParadoxScriptBlockElement ?: return
			//进行提示
			CwtConfigHandler.addValueCompletionsInBlock(valueElement, blockElement, context, resultToUse)
		}
	}
}
