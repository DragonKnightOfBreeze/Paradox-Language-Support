package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.expression.ParadoxCommandExpression
import icu.windea.pls.lang.expression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.expression.getAllReferences
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

class ParadoxLocalisationCommandExpressionSupport : ParadoxLocalisationExpressionSupport {
    override fun supports(element: ParadoxExpressionElement): Boolean {
        return when (element) {
            is ParadoxScriptExpressionElement -> false //NOTE 1.4.0 - unnecessary to support yet
            is ParadoxLocalisationExpressionElement -> element.isCommandExpression()
            else -> false
        }
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(element))
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val commandExpression = ParadoxCommandExpression.resolve(value, textRange, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, commandExpression, holder)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(element))
        val range = TextRange.create(0, expressionText.length)
        val commandExpression = ParadoxCommandExpression.resolve(expressionText, range, configGroup)
        if (commandExpression == null) return PsiReference.EMPTY_ARRAY
        return commandExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeCommandExpression(context, result)
    }
}

class ParadoxLocalisationDatabaseObjectExpressionSupport : ParadoxLocalisationExpressionSupport {
    override fun supports(element: ParadoxExpressionElement): Boolean {
        return when (element) {
            is ParadoxScriptExpressionElement -> false //NOTE 1.4.0 - unnecessary to support yet
            is ParadoxLocalisationExpressionElement -> element.isDatabaseObjectExpression()
            else -> false
        }
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {
        if(element is ParadoxLocalisationExpressionElement && !element.isDatabaseObjectExpression(strict = true)) return

        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(element))
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(value, textRange, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, databaseObjectExpression, holder)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
        if(element is ParadoxLocalisationExpressionElement && !element.isDatabaseObjectExpression(strict = true)) return null

        val configGroup = PlsFacade.getConfigGroup(element.project, selectGameType(element))
        val range = TextRange.create(0, expressionText.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expressionText, range, configGroup)
        if (databaseObjectExpression == null) return PsiReference.EMPTY_ARRAY
        return databaseObjectExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}
