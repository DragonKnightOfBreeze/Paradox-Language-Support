package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.configGroup
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType

object ParadoxExpressionService {
    // NOTE recursion guard is required for script expression resolution

    // region Script Expression Related

    /**
     * @see ParadoxScriptExpressionSupport.annotate
     */
    fun annotateScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        // NOTE recursion guard is required here
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
                ProgressManager.checkCanceled()
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
    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        // NOTE recursion guard is required here
        val configExpression = config.configExpression ?: return null
        val gameType = config.configGroup.gameType
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
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
    fun multiResolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Collection<PsiElement> {
        // NOTE recursion guard is required here
        val configExpression = config.configExpression ?: return emptySet()
        val gameType = config.configGroup.gameType
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
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
    fun getScriptExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): Array<out PsiReference>? {
        // NOTE recursion guard is required here
        val configExpression = config.configExpression ?: return null
        val gameType = config.configGroup.gameType
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
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
    fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet) {
        // NOTE recursion guard is required here
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                withRecursionCheck("${ep.javaClass.name}@complete${context.keyword}") {
                    ep.complete(context, result)
                }
            }
        }
    }

    // endregion

    // region Localisation Expression Related

    /**
     * @see ParadoxLocalisationExpressionSupport.annotate
     */
    fun annotateLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder) {
        val gameType = selectGameType(element)
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.annotate(element, rangeInElement, expressionText, holder)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolve
     */
    fun resolveLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): PsiElement? {
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.resolve(element, rangeInElement, expressionText)
            r
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.multiResolve
     */
    fun multiResolveLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Collection<PsiElement> {
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.multiResolve(element, rangeInElement, expressionText).orNull()
            r
        }.orEmpty()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.getReferences
     */
    fun getLocalisationExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): Array<out PsiReference>? {
        val gameType = selectGameType(element)
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            val r = ep.getReferences(element, rangeInElement, expressionText).orNull()
            if (r != null) return r
        }
        return null
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.complete
     */
    fun completeLocalisationExpression(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val configGroup = context.configGroup ?: return
        val gameType = configGroup.gameType
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.complete(context, result)
        }
    }

    // endregion

    // region Csv Expression Related

    /**
     * @see ParadoxCsvExpressionSupport.annotate
     */
    fun annotateCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtValueConfig) {
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        ParadoxCsvExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.annotate(element, rangeInElement, expressionText, holder, config)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolve
     */
    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): PsiElement? {
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.resolve(element, rangeInElement, expressionText, config)
            r
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.multiResolve
     */
    fun multiResolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): Collection<PsiElement> {
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            val r = ep.multiResolve(element, rangeInElement, expressionText, config).orNull()
            r
        }.orEmpty()
    }

    /**
     * @see ParadoxCsvExpressionSupport.complete
     */
    fun completeCsvExpression(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config?.castOrNull<CwtValueConfig>() ?: return
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        ParadoxCsvExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            ep.complete(context, result)
        }
    }

    // endregion
}
