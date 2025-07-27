package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.model.constants.*

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

