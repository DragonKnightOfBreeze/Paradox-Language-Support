package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.editor.*
import icu.windea.pls.script.psi.*

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
