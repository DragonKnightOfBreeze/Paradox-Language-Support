package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

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
