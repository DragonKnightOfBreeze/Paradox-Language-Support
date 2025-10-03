package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * Stellaris 命名格式表达式的脚本支持（高亮/引用/补全）。
 */
class ParadoxScriptStellarisNameFormatExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.StellarisNameFormat
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val expr = StellarisNameFormatExpression.resolve(expressionText, range, configGroup, config) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, expr, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val expr = StellarisNameFormatExpression.resolve(expressionText, range, configGroup, config)
        if (expr == null) return PsiReference.EMPTY_ARRAY
        return expr.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        // 迭代实现：先不提供特定补全，后续补充 <...> 定义与本地化调用的智能提示
    }
}
