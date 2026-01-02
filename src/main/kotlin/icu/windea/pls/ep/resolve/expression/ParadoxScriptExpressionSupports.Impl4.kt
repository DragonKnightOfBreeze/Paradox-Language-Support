package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.unquote
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxLocalisationParameterService
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// Parameter

/**
 * @see CwtDataTypes.Parameter
 */
class ParadoxScriptParameterExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Parameter
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return // only for string expressions in script files
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null // only for string expressions in script files
        return ParadoxParameterService.resolveArgument(element, rangeInElement, config)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeParameter(context, result)
    }
}

/**
 * @see CwtDataTypes.LocalisationParameter
 */
class ParadoxScriptLocalisationParameterExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.LocalisationParameter
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return // only for string expressions in script files
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null // only for string expressions in script files
        return ParadoxLocalisationParameterService.resolveArgument(element, rangeInElement, config)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        // NOTE 不兼容本地化参数（CwtDataTypes.LocalisationParameter），因为那个引用实际上也可能对应一个缺失的本地化的名字
    }
}
