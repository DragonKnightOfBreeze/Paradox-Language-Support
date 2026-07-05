package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import icu.windea.pls.model.constants.ChronicleConstants

class ParadoxCsvCompletionContributor : CompletionContributor() {
    init {
        ParadoxCsvExpressionCompletionProvider().let { extend(null, it.elementPattern, it) }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = ChronicleConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}
