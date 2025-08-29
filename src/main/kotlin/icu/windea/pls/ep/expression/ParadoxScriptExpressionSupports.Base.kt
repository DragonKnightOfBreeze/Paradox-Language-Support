package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addBlockScriptExpressionElement
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement

class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.Block
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return config.pointer.element
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addBlockScriptExpressionElement(context)
    }
}

class ParadoxScriptBoolExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.Bool
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(ParadoxCompletionManager.yesLookupElement, context)
        result.addElement(ParadoxCompletionManager.noLookupElement, context)
    }
}
