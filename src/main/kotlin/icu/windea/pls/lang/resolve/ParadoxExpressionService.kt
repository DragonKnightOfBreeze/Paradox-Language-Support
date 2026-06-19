package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.base.annotations.ChronicleAnnotationManager
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.type.ParadoxExpressionRole

object ParadoxExpressionService {
    // NOTE recursion guard is required for script expression resolution

    // region Script Expression Related

    /**
     * @see ParadoxScriptExpressionSupport.annotate
     */
    fun annotateScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression is empty
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f
                if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
                withRecursionCheck("${ep.javaClass.name}@a@${text}") {
                    ep.annotate(element, rangeInElement, text, config, holder)
                }
            }
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolve
     */
    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression is empty
        val configExpression = config.configExpression ?: return null
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f null
                if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
                withRecursionCheck("${ep.javaClass.name}@r@${text}") {
                    ep.resolve(element, rangeInElement, text, config, role)
                }
            }
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolveAll
     */
    fun resolveAllScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression ?: return emptyList()
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f null
                if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
                withRecursionCheck("${ep.javaClass.name}@ra@${text}") {
                    ep.resolveAll(element, rangeInElement, text, config, role).orNull()
                }
            }
        }.orEmpty()
    }

    /**
     * @see ParadoxScriptExpressionSupport.getReferences
     */
    fun getScriptExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression ?: return emptyList()
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f null
                if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
                withRecursionCheck("${ep.javaClass.name}@gr@${text}") {
                    ep.getReferences(element, rangeInElement, text, config, role).orNull()
                }
            }
        }.orEmpty()
    }

    /**
     * @see ParadoxScriptExpressionSupport.complete
     */
    fun completeScriptExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val gameType = config.configGroup.gameType
        // NOTE recursion guard is required here
        withRecursionGuard {
            ParadoxScriptExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
                ProgressManager.checkCanceled()
                if (!ep.supports(config, configExpression)) return@f
                if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
                withRecursionCheck("${ep.javaClass.name}@c@${context.keyword}") {
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
    fun annotateLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression is empty
        val gameType = selectGameType(element)
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.annotate(element, rangeInElement, text, holder)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolve
     */
    fun resolveLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression is empty
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
            ep.resolve(element, rangeInElement, text)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolveAll
     */
    fun resolveAllLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
            ep.resolveAll(element, rangeInElement, text).orNull()
        }.orEmpty()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.getReferences
     */
    fun getLocalisationExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiReference> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val gameType = selectGameType(element)
        return ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f null
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
            ep.getReferences(element, rangeInElement, text).orNull()
        }.orEmpty()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.complete
     */
    fun completeLocalisationExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val configGroup = context.configGroup ?: return
        val gameType = configGroup.gameType
        ParadoxLocalisationExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(element)) return@f
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.complete(context, result)
        }
    }

    // endregion

    // region Csv Expression Related

    /**
     * @see ParadoxCsvExpressionSupport.annotate
     */
    fun annotateCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression is empty
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        ParadoxCsvExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.annotate(element, rangeInElement, text, config, holder)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolve
     */
    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression is empty
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f null
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
            ep.resolve(element, rangeInElement, text, config)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolveAll
     */
    fun resolveAllCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression
        val gameType = config.configGroup.gameType
        return ParadoxCsvExpressionSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f null
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f null
            ep.resolveAll(element, rangeInElement, text, config).orNull()
        }.orEmpty()
    }

    /**
     * @see ParadoxCsvExpressionSupport.complete
     */
    fun completeCsvExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config?.castOrNull<CwtValueConfig>() ?: return
        val configExpression = config.configExpression
        val gameType = context.gameType
        ParadoxCsvExpressionSupport.EP_NAME.extensionList.forEach f@{ ep ->
            ProgressManager.checkCanceled()
            if (!ep.supports(config, configExpression)) return@f
            if (!ChronicleAnnotationManager.check(ep, gameType)) return@f
            ep.complete(context, result)
        }
    }

    // endregion
}
