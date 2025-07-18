package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*

/**
 * 提供定义参数的代码补全。
 */
class ParadoxParameterCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        //对于定义声明中的`$PARAM$`引用和`[[PARAM] ... ]`引用
        val tokenElement = parameters.position
        val element = tokenElement.parent
        val offsetInParent = parameters.offset - tokenElement.startOffset
        val keyword = tokenElement.getKeyword(offsetInParent)

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword

        ParadoxParameterManager.completeParameters(element, context, result)
    }
}
