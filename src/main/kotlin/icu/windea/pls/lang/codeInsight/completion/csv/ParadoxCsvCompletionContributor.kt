package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.psiElement
import icu.windea.pls.core.extend
import icu.windea.pls.csv.psi.ParadoxCsvTokenSets
import icu.windea.pls.model.constants.PlsConstants

class ParadoxCsvCompletionContributor : CompletionContributor() {
    init {
        val expressionPattern = psiElement().withElementType(ParadoxCsvTokenSets.EXPRESSION_TOKENS)
        extend(expressionPattern, ParadoxCsvExpressionCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}

