package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.unquote
import icu.windea.pls.ep.parameter.ParadoxLocalisationParameterSupport
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxScriptParameterExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.Parameter
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return //only for string expressions in script files
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null //only for string expressions in script files
        return ParadoxParameterSupport.resolveArgument(element, rangeInElement, config)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeParameter(context, result)
    }
}

class ParadoxScriptLocalisationParameterExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.LocalisationParameter
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return //only for string expressions in script files
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null //only for string expressions in script files
        return ParadoxLocalisationParameterSupport.resolveArgument(element, rangeInElement, config)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //NOTE 不兼容本地化参数（CwtDataTypes.LocalisationParameter），因为那个引用实际上也可能对应一个缺失的本地化的名字
    }
}
