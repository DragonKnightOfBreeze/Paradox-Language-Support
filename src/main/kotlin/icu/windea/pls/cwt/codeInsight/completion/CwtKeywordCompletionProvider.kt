package icu.windea.pls.cwt.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.codeInsight.completion.*

/**
 * 提供关键字的代码补全（要求不在规则文件中提供）。
 */
class CwtKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val lookupElements = listOf(
        CwtConfigCompletionManager.yesLookupElement,
        CwtConfigCompletionManager.noLookupElement,
        CwtConfigCompletionManager.blockLookupElement,
    )
    
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val contextElement = position.parent?.castOrNull<CwtString>() ?: return
        if(contextElement.text.isLeftQuoted()) return
        
        //判断当前文件是否是规则文件
        
        ParadoxCompletionManager.initializeContext(parameters, context)
        
        val r = CwtConfigCompletionManager.initializeContext(parameters, context, contextElement)
        if(r) return
        
        lookupElements.forEach { lookupElement ->
            result.addElement(lookupElement, context)
        }
    }
}
