package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addBlockScriptExpressionElement
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement

// Base

/**
 * @see CwtDataTypes.Bool
 */
class ParadoxScriptBoolExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Bool
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(ParadoxCompletionManager.yesLookupElement, context)
        result.addElement(ParadoxCompletionManager.noLookupElement, context)
    }
}

/**
 * @see CwtDataTypes.Block
 */
class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Block
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return config.pointer.element
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addBlockScriptExpressionElement(context)
    }
}
