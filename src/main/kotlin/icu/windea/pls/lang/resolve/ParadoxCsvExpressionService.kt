package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.codeInsight.completion.config

object ParadoxCsvExpressionService {
    /**
     * @see ParadoxCsvExpressionSupport.annotate
     */
    fun annotate(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        ParadoxCsvExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!ep.supports(config, configExpression)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.annotate(element, rangeInElement, expressionText, holder, config)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolve
     */
    fun resolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(config, configExpression)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.resolve(element, rangeInElement, expressionText, config)
            r
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.multiResolve
     */
    fun multiResolve(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): Collection<PsiElement> {
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(config, configExpression)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.multiResolve(element, rangeInElement, expressionText, config).orNull()
            r
        }.orEmpty()
    }

    /**
     * @see ParadoxCsvExpressionSupport.complete
     */
    fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config?.castOrNull<CwtValueConfig>() ?: return
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        ParadoxCsvExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            if (!ep.supports(config, configExpression)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.complete(context, result)
        }
    }
}
