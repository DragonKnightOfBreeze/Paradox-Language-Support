package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxClauseTemplateCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionLookupProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.type.ParadoxExpressionRole

// Basic

/**
 * @see CwtDataTypes.Bool
 */
class ParadoxScriptBoolExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Bool
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxCompletionLookupProvider.forBool().addToResult(context, result)
    }
}

/**
 * @see CwtDataTypes.Block
 */
class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Block
    }

    override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        return config.pointer.element
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxCompletionLookupProvider.forBlockKeyword().addToResult(context, result)

        // 进行提示并在提示后插入子句内联模板（仅当子句中允许键为常量字符串的属性时才会提示）
        val config = context.config!!
        val extraLookupElement = ParadoxClauseTemplateCompletionManager.buildBlockLookupElement(context, config)
        extraLookupElement.addToResult(context, result)
    }
}
