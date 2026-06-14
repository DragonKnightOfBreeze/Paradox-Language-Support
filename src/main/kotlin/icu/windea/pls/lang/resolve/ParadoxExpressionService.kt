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
        if (expressionText.isEmpty()) return // skip if expression is empty
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
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
    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): PsiElement? {
        if (expressionText.isEmpty()) return null // ignore if expression is empty
        val configExpression = config.configExpression ?: return null
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f null
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                val r = withRecursionCheck("${ep.javaClass.name}@resolve@${expressionText}") {
                    ep.resolve(element, rangeInElement, expressionText, config, isKey)
                }
                r
            }
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolveAll
     */
    fun resolveAllScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): List<PsiElement> {
        if (expressionText.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression ?: return emptyList()
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f null
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                withRecursionCheck("${ep.javaClass.name}@multiResolve@${expressionText}") {
                    ep.resolveAll(element, rangeInElement, expressionText, config, isKey).orNull()
                }
            }
        }.orEmpty()
    }

    /**
     * @see ParadoxScriptExpressionSupport.getReferences
     */
    fun getScriptExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean? = null): List<PsiReference> {
        if (expressionText.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression ?: return emptyList()
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f null
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                withRecursionCheck("${ep.javaClass.name}@multiResolve@${expressionText}") {
                    ep.getReferences(element, rangeInElement, expressionText, config, isKey).orNull()
                }
            }
        }.orEmpty()
    }

    /**
     * @see ParadoxScriptExpressionSupport.complete
     */
    fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
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
        if (expressionText.isEmpty()) return // skip if expression is empty
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
        if (expressionText.isEmpty()) return null // ignore if expression is empty
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
     * @see ParadoxLocalisationExpressionSupport.resolveAll
     */
    fun resolveAllLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): List<PsiElement> {
        if (expressionText.isEmpty()) return emptyList() // ignore if expression is empty
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.resolveAll(element, rangeInElement, expressionText).orNull()
        }.orEmpty()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.getReferences
     */
    fun getLocalisationExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String): List<PsiReference> {
        if (expressionText.isEmpty()) return emptyList() // ignore if expression is empty
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getReferences(element, rangeInElement, expressionText).orNull()
        }.orEmpty()
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
        if (expressionText.isEmpty()) return // skip if expression is empty
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
        if (expressionText.isEmpty()) return null // ignore if expression is empty
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
     * @see ParadoxCsvExpressionSupport.resolveAll
     */
    fun resolveAllCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtValueConfig): List<PsiElement> {
        if (expressionText.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f null
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.resolveAll(element, rangeInElement, expressionText, config).orNull()
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
