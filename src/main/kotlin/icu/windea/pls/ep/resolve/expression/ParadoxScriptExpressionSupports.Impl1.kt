package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxClauseTemplateCompletionManager
import icu.windea.pls.lang.codeInsight.completion.PlsLookupElements
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.config
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
        result.addElement(PlsLookupElements.yesLookupElement, context)
        result.addElement(PlsLookupElements.noLookupElement, context)
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
        result.addElement(PlsLookupElements.blockLookupElement, context)

        // 进行提示并在提示后插入子句内联模板（仅当子句中允许键为常量字符串的属性时才会提示）
        val config = context.config!!
        val extraLookupElement = ParadoxClauseTemplateCompletionManager.buildBlockLookupElement(context, config)
        result.addElement(extraLookupElement, context)
    }
}
