package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionLookupProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult

// Basic

/**
 * @see CwtDataTypes.Bool
 */
class ParadoxCsvBoolExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Bool
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxCompletionLookupProvider.forBool().addToResult(context, result)
    }
}
