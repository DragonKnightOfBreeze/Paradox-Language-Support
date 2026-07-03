package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.isComplexExpression
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets.EXPRESSION_TOKENS

/**
 * 提供本地化表达式相关的代码补全。基于规则文件。
 */
class ParadoxLocalisationExpressionCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(EXPRESSION_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parentOfType<ParadoxLocalisationExpressionElement>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return
        if (!element.isComplexExpression()) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxExpressionCompletionManager.completeLocalisationExpression(context, result)
    }
}
