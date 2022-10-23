package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeParameters
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义参数的代码补全。对于定义声明中的`$PARAM$`引用和`[[PARAM] ... ]`引用。
 */
class ParadoxParameterCompletionProvider: CompletionProvider<CompletionParameters>(){
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val tokenElement = parameters.position
		val parameterElement = tokenElement.parent
		val read = when(parameterElement){
			is ParadoxParameter -> true
			is ParadoxArgument -> false
			else -> return
		}
		
		val offsetInParent = parameters.offset - tokenElement.textRange.startOffset
		val keyword = tokenElement.getKeyword(offsetInParent)
		
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		
		context.completeParameters(parameterElement, read, result)
	}
}