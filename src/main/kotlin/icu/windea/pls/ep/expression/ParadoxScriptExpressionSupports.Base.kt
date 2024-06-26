package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.script.psi.*

class ParadoxBlockScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Block
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return config.pointer.element
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addBlockScriptExpressionElement(context)
    }
}

class ParadoxBoolScriptExpressionSupport: ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Bool
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        result.addSimpleScriptExpressionElement(ParadoxLookupElements.yesLookupElement, context)
        result.addSimpleScriptExpressionElement(ParadoxLookupElements.noLookupElement, context)
    }
}
