package icu.windea.pls.ep.resolve.expression

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

abstract class ParadoxScriptExpressionSupportBase : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>, configExpression: CwtDataExpression): Boolean {
        return supports(configExpression.type)
    }

    protected open fun supports(dataType: CwtDataType): Boolean = false
}

/**
 * @see ParadoxComplexExpression
 */
abstract class ParadoxScriptComplexExpressionSupportBase : ParadoxScriptExpressionSupportBase() {
    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val complexExpression = ParadoxComplexExpression.resolveByConfig(expressionText, null, configGroup, config) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, complexExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): List<PsiReference> {
        if (element !is ParadoxScriptStringExpressionElement) return emptyList()
        val configGroup = config.configGroup
        val complexExpression = ParadoxComplexExpression.resolveByConfig(expressionText, null, configGroup, config) ?: return emptyList()
        val references = complexExpression.getAllReferences(element)
        if (references.isEmpty()) return emptyList()
        return references
    }
}
