package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.isCommandExpression
import icu.windea.pls.lang.psi.isDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

// Complex Expressions

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
