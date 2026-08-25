package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionFactory
import icu.windea.pls.lang.codeInsight.completion.addToResult

// Basic

/**
 * @see CwtDataTypes.Bool
 */
class ParadoxCsvBoolExpressionSupport : ParadoxCsvExpressionSupport {
    override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Bool

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxCompletionFactory.forBool().addToResult(context, result)
    }
}
