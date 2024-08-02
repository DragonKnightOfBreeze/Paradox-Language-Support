package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.expression.complex.*

class ParadoxLocalisationCommandExpressionSupport: ParadoxLocalisationExpressionSupport {
    override fun supports(element: ParadoxLocalisationExpressionElement): Boolean {
        return element.isCommandExpression()
    }
    
    override fun annotate(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder) {
        val configGroup = getConfigGroup(element.project, selectGameType(element))
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val commandExpression = ParadoxCommandExpression.resolve(value, textRange, configGroup) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, commandExpression, holder)
    }
    
    override fun getReferences(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): Array<out PsiReference>? {
        val configGroup = getConfigGroup(element.project, selectGameType(element))
        val range = TextRange.create(0, expression.length)
        val commandExpression = ParadoxCommandExpression.resolve(expression, range, configGroup)
        if(commandExpression == null) return PsiReference.EMPTY_ARRAY
        return commandExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeComplexExpression(context, result, ParadoxCommandExpression::class.java)
    }
}

class ParadoxLocalisationDatabaseObjectExpressionSupport: ParadoxLocalisationExpressionSupport {
    override fun supports(element: ParadoxLocalisationExpressionElement): Boolean {
        return element.isDatabaseObjectExpression()
    }
    
    override fun annotate(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder) {
        val configGroup = getConfigGroup(element.project, selectGameType(element))
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(value, textRange, configGroup) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, databaseObjectExpression, holder)
    }
    
    override fun getReferences(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, expression: String): Array<out PsiReference>? {
        val configGroup = getConfigGroup(element.project, selectGameType(element))
        val range = TextRange.create(0, expression.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expression, range, configGroup)
        if(databaseObjectExpression == null) return PsiReference.EMPTY_ARRAY
        return databaseObjectExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeComplexExpression(context, result, ParadoxDatabaseObjectExpression::class.java)
    }
}
