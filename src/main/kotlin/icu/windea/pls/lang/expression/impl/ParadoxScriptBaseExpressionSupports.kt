package icu.windea.pls.lang.expression.impl

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.script.psi.*

class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Block
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return config.pointer.element
    }
    
    override fun complete(config: CwtConfig<*>, context: ProcessingContext, result: CompletionResultSet) {
        result.addBlockElement(context)
    }
}

class ParadoxScriptBoolExpressionSupport: ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Bool
    }
    
    override fun complete(config: CwtConfig<*>, context: ProcessingContext, result: CompletionResultSet) {
        result.addExpressionElement(context, PlsLookupElements.yesLookupElement)
        result.addExpressionElement(context, PlsLookupElements.noLookupElement)
    }
}