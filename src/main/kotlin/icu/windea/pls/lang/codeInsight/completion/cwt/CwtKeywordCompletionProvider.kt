package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtTokenSets
import icu.windea.pls.lang.codeInsight.completion.CwtCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager
import icu.windea.pls.lang.codeInsight.completion.PlsLookupElements
import icu.windea.pls.lang.codeInsight.completion.addElements

object CwtKeywordCompletionProvider : CwtCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(CwtTokenSets.STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val contextElement = position.parent?.castOrNull<CwtString>() ?: return
        if (contextElement.text.isLeftQuoted()) return

        val r = CwtConfigCompletionManager.initializeContext(contextElement, parameters, context)
        if (r) return

        result.addElements(PlsLookupElements.keywordLookupElements, context)
    }
}
