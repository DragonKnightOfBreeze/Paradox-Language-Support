package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.isCommandExpression
import icu.windea.pls.lang.psi.isDatabaseObjectExpression
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.providers.ParadoxAnnotateProvider
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

// Complex Expressions

/**
 * @see ParadoxComplexExpression
 */
abstract class ParadoxLocalisationComplexExpressionSupportBase : ParadoxLocalisationExpressionSupport {
    // NOTE 2.0.6 - unnecessary to support for `ParadoxScriptExpressionElement` yet

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, holder: AnnotationHolder) {
        if (element !is ParadoxLocalisationExpressionElement) return
        val configGroup = ChronicleFacade.getConfigGroup(element.project, selectGameType(element))
        val offset = ParadoxExpressionService.getExpressionOffset(element)
        val rangeInExpression = rangeInElement?.shiftLeft(offset) // #390
        val complexExpression = ParadoxComplexExpression.resolve(element, rangeInExpression, configGroup) ?: return
        ParadoxAnnotateProvider.annotateComplexExpression(element, complexExpression, holder)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiReference> {
        if (element !is ParadoxLocalisationExpressionElement) return emptyList()
        val configGroup = ChronicleFacade.getConfigGroup(element.project, selectGameType(element))
        val offset = ParadoxExpressionService.getExpressionOffset(element)
        val rangeInExpression = rangeInElement?.shiftLeft(offset) // #390
        val complexExpression = ParadoxComplexExpression.resolve(element, rangeInExpression, configGroup) ?: return emptyList()
        val references = complexExpression.getAllReferences(element)
        if (references.isEmpty()) return emptyList()
        return references
    }
}

/**
 * @see ParadoxCommandExpression
 */
class ParadoxLocalisationCommandExpressionSupport : ParadoxLocalisationComplexExpressionSupportBase() {
    override fun supports(element: ParadoxExpressionElement): Boolean {
        return element is ParadoxLocalisationExpressionElement && element.isCommandExpression()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeCommandExpression(context, result)
    }
}

/**
 * @see ParadoxDatabaseObjectExpression
 */
class ParadoxLocalisationDatabaseObjectExpressionSupport : ParadoxLocalisationComplexExpressionSupportBase() {
    override fun supports(element: ParadoxExpressionElement): Boolean {
        return element is ParadoxLocalisationExpressionElement && element.isDatabaseObjectExpression()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}
