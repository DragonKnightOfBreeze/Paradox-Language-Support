package icu.windea.pls.lang.expression

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.codeInsight.completion.*

class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Block
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