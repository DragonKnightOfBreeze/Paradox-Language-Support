package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtTokenSets.STRING_TOKENS
import icu.windea.pls.lang.codeInsight.completion.CwtCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionContext
import icu.windea.pls.lang.codeInsight.completion.PlsLookupElements
import icu.windea.pls.lang.codeInsight.completion.addElements

/**
 * 提供关键字的代码补全（要求不在规则文件中提供）。
 */
object CwtKeywordCompletionProvider : CwtCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val contextElement = position.parent?.castOrNull<CwtString>() ?: return
        if (contextElement.text.isLeftQuoted()) return

        val globalContext = GlobalCompletionContext.create(contextElement, parameters, context)
        val context = CwtConfigCompletionContext.create(globalContext)
        if (context != null) return

        result.addElements(PlsLookupElements.keywordLookupElements, globalContext)
    }
}
