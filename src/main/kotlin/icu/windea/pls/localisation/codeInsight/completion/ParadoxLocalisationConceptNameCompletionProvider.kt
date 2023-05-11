package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.*

/**
 * 提供概念的名字和别名的代码补全。
 */
class ParadoxLocalisationConceptNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent ?: return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        val file = parameters.originalFile
        
        context.put(PlsCompletionKeys.parametersKey, parameters)
        context.put(PlsCompletionKeys.contextElementKey, element)
        context.put(PlsCompletionKeys.originalFileKey, file)
        context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
        context.put(PlsCompletionKeys.keywordKey, keyword)
        
        //提示concept的name或alias
        ParadoxConfigHandler.completeConcept(context, result)
    }
}