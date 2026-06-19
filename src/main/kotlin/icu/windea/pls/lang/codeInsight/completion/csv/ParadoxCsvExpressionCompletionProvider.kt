package icu.windea.pls.lang.codeInsight.completion.csv

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvTokenSets.EXPRESSION_TOKENS
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxCsvCompletionManager

/**
 * 提供 CSV 表达式相关的代码补全。基于规则文件。
 */
class ParadoxCsvExpressionCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(EXPRESSION_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxCsvColumn>() ?: return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxCsvCompletionManager.addColumnCompletions(element, context, result)
    }
}
