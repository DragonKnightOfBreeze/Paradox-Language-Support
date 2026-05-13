package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.codeInsight.completion.PlsLookupElements
import icu.windea.pls.lang.codeInsight.completion.addElement

// Base

/**
 * @see CwtDataTypes.Bool
 */
class ParadoxCsvBoolExpressionSupport : ParadoxCsvExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Bool
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(PlsLookupElements.yesLookupElement, context)
        result.addElement(PlsLookupElements.noLookupElement, context)
    }
}

