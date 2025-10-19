package icu.windea.pls.ep.expression

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

abstract class ParadoxLocalisationComplexExpressionSupportBase : ParadoxLocalisationExpressionSupport {
    // NOTE 2.0.6 - unnecessary to support for `ParadoxScriptExpressionElement` yet

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {
        if (element !is ParadoxLocalisationExpressionElement) return
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(element))
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, complexExpression, holder)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
        if (element !is ParadoxLocalisationExpressionElement) return null
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(element))
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup) ?: return null
        val references = complexExpression.getAllReferences(element)
        if (references.isEmpty()) return null
        return references.toTypedArray()
    }
}
