package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.psi.ParadoxExpressionElement

object ParadoxScriptExpressionService {
    // 这里需要尝试避免 SOE

    /**
     * @see ParadoxScriptExpressionSupport.annotate
     */
    fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(config, configExpression)) return@f
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                withRecursionCheck("${ep.javaClass.name}@annotate@${expressionText}") {
                    ep.annotate(element, rangeInElement, expressionText, holder, config)
                }
            }
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolve
     */
    fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        val configExpression = config.configExpression ?: return null
        val gameType = config.configGroup.gameType
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(config, configExpression)) return@f null
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                val r = withRecursionCheck("${ep.javaClass.name}@resolve@${expressionText}") {
                    ep.resolve(element, rangeInElement, expressionText, config, isKey, exact)
                }
                r
            }
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.multiResolve
     */
    fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
        val configExpression = config.configExpression ?: return emptySet()
        val gameType = config.configGroup.gameType
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(config, configExpression)) return@f null
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                val r = withRecursionCheck("${ep.javaClass.name}@multiResolve@${expressionText}") {
                    ep.multiResolve(element, rangeInElement, expressionText, config, isKey).orNull()
                }
                r
            }
        }.orEmpty()
    }

    /**
     * @see ParadoxScriptExpressionSupport.getReferences
     */
    fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
        val configExpression = config.configExpression ?: return null
        val gameType = config.configGroup.gameType
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(config, configExpression)) return@f null
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                val r = withRecursionCheck("${ep.javaClass.name}@multiResolve@${expressionText}") {
                    ep.getReferences(element, rangeInElement, expressionText, config, isKey).orNull()
                }
                r
            }
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.complete
     */
    fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
                if (!ep.supports(config, configExpression)) return@f
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                withRecursionCheck("${ep.javaClass.name}@complete${context.keyword}") {
                    ep.complete(context, result)
                }
            }
        }
    }
}
