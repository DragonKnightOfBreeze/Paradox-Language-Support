package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement

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
        if (contextElement.text.isLeftQuoted()) return

        val r = CwtConfigCompletionManager.initializeContext(contextElement, parameters, context)
        if (r) return

        lookupElements.forEach { lookupElement ->
            result.addElement(lookupElement, context)
        }
    }
}
