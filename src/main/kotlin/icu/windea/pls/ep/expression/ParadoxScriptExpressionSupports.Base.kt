package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.ParadoxExpressionElement

class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Block
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
        return config.expression?.type == CwtDataTypes.Bool
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(ParadoxCompletionManager.yesLookupElement, context)
        result.addElement(ParadoxCompletionManager.noLookupElement, context)
    }
}
