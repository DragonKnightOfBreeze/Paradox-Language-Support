package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*

/**
 * 提供概念的名字和别名的代码补全。
 */
class ParadoxLocalisationConceptCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent ?: return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        val file = parameters.originalFile
        
        context.parameters = parameters
        context.contextElement = element
        context.originalFile = file
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        
        //提示concept的name或alias
        ParadoxCompletionManager.completeConcept(context, result)
    }
}