package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.model.constants.*

class ParadoxCsvCompletionContributor : CompletionContributor() {
    init {
        val headerColumnPattern = psiElement(COLUMN_TOKEN).withParent(psiElement(COLUMN).withParent(psiElement(HEADER)))
        extend(headerColumnPattern, ParadoxCsvHeaderColumnCompletionProvider())

        val columnPattern = psiElement(COLUMN_TOKEN).withParent(psiElement(COLUMN).withParent(psiElement(ROW)))
        extend(headerColumnPattern, ParadoxCsvColumnCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}

