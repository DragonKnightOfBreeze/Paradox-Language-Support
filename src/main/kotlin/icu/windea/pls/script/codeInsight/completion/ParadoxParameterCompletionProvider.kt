package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*

/**
 * 提供定义参数的代码补全。
 */
class ParadoxParameterCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//对于定义声明中的`$PARAM$`引用和`[[PARAM] ... ]`引用
		val tokenElement = parameters.position
		val element = tokenElement.parent
		val offsetInParent = parameters.offset - tokenElement.startOffset
		val file = parameters.originalFile
		val keyword = tokenElement.getKeyword(offsetInParent)
		
		context.completionIds = mutableSetOf<String>().synced()
		context.parameters = parameters
		context.contextElement = element
		context.originalFile = file
		context.offsetInParent = offsetInParent
		context.keyword = keyword
		
		ParadoxParameterHandler.completeParameters(element, context, result)
	}
}
