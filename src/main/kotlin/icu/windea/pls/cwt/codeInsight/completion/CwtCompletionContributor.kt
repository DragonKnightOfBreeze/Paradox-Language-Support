package icu.windea.pls.cwt.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.constants.PlsConstants

class CwtCompletionContributor : CompletionContributor() {
    init {
        val keywordPattern = psiElement()
            .withElementType(CwtTokenSets.STRING_TOKENS)
        extend(CompletionType.BASIC, keywordPattern, CwtKeywordCompletionProvider())

        val configPattern = psiElement()
            .withElementType(CwtTokenSets.KEY_OR_STRING_TOKENS)
        extend(configPattern, CwtConfigCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}
