package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ChronicleLookupElements
import icu.windea.pls.lang.codeInsight.completion.ParadoxClauseTemplateCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.type.ParadoxExpressionRole

// Basic

/**
 * @see CwtDataTypes.Bool
 */
class ParadoxScriptBoolExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Bool
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        result.addElement(ChronicleLookupElements.yesLookupElement, context)
        result.addElement(ChronicleLookupElements.noLookupElement, context)
    }
}

/**
 * @see CwtDataTypes.Block
 */
class ParadoxScriptBlockExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Block
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        return config.pointer.element
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        result.addElement(ChronicleLookupElements.blockLookupElement, context)

        // 进行提示并在提示后插入子句内联模板（仅当子句中允许键为常量字符串的属性时才会提示）
        val config = context.config!!
        val extraLookupElement = ParadoxClauseTemplateCompletionManager.buildBlockLookupElement(context, config)
        result.addElement(extraLookupElement, context)
    }
}
