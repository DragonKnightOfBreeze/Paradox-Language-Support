package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的相关代码补全。基于CWT规则文件。
 */
class ParadoxDefinitionCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val element = parameters.position.parent ?: return
		
		ProgressManager.checkCanceled()
		
		val file = parameters.originalFile
		val quoted = element.text.isLeftQuoted()
		val rightQuoted = element.text.isRightQuoted()
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.contextElementKey, element)
		context.put(PlsCompletionKeys.originalFileKey, file)
		context.put(PlsCompletionKeys.quotedKey, quoted)
		context.put(PlsCompletionKeys.rightQuotedKey, rightQuoted)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		
		val resultToUse = result.withPrefixMatcher(keyword)
		
		val mayBeKey = element is ParadoxScriptPropertyKey || (element is ParadoxScriptValue && element.isBlockValue())
		val mayBePropertyValue = element is ParadoxScriptString && element.isPropertyValue()
		val mayBeValue = element is ParadoxScriptString && element.isBlockValue()
		
		if(mayBeValue) {
			//得到原始文件中上一级block
			val blockElement = element.parent as? ParadoxScriptBlockElement
			val memberElement = if(blockElement is ParadoxScriptBlock) blockElement else blockElement?.parent as? ParadoxScriptFile
			if(memberElement != null) {
				//进行提示
				CwtConfigHandler.addValueCompletions(memberElement, context, resultToUse)
			}
		}
		if(mayBeKey) {
			//得到上一级definitionElement（跳过可能正在填写的definitionElement）
			val definitionElement = element.findParentProperty(fromParentBlock = true) 
			if(definitionElement != null) {
				//进行提示
				CwtConfigHandler.addKeyCompletions(definitionElement, context, resultToUse)
			}
		}
		if(mayBePropertyValue) {
			//得到原始文件中上一级definitionElement
			val definitionElement = element.findParentProperty() 
			if(definitionElement !=  null) {
				//这里需要特殊处理一下，标记属性的值是否未填写
				val incomplete = !quoted && keyword.isEmpty()
				try {
					definitionElement.putUserData(PlsKeys.isIncompleteKey, incomplete)
					//进行提示
					CwtConfigHandler.addPropertyValueCompletions(definitionElement, context, resultToUse)
				} finally {
					definitionElement.putUserData(PlsKeys.isIncompleteKey, null)
				}
			}
		}
	}
}
