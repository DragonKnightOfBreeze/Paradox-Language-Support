package icu.windea.pls.ep.expression

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

abstract class ParadoxScriptComplexExpressionSupportBase : ParadoxScriptExpressionSupport {
    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val complexExpression = ParadoxComplexExpression.resolveByConfig(expressionText, range, configGroup, config) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, complexExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return null
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val complexExpression = ParadoxComplexExpression.resolveByConfig(expressionText, range, configGroup, config) ?: return null
        val references = complexExpression.getAllReferences(element)
        if (references.isEmpty()) return null
        return references.toTypedArray()
    }
}
