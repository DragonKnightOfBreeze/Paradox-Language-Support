package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression

/**
 * @see ParadoxCommandExpression
 */
class ParadoxLocalisationCommandExpressionSupport : ParadoxLocalisationComplexExpressionSupportBase() {
    override fun supports(element: ParadoxExpressionElement): Boolean {
        return element is ParadoxLocalisationExpressionElement && element.isCommandExpression()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
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

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}
