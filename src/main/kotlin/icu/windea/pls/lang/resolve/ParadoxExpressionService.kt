package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.base.annotations.ChronicleAnnotationService
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isEmpty
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.CwtMemberConfigLightElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.type.ParadoxExpressionRole

@Optimized
object ParadoxExpressionService {
    // region Common Methods

    fun getResolvedConfigElement(element: ParadoxExpressionElement, config: CwtConfig<*>, configGroup: CwtConfigGroup): PsiElement? {
        val resolvedConfig = config.resolved()
        if (resolvedConfig is CwtMemberConfig<*> && resolvedConfig.pointer.isEmpty()) {
            // 特殊处理合成的规则
            val gameType = configGroup.gameType
            val project = configGroup.project
            return CwtMemberConfigLightElement(element, resolvedConfig, gameType, project)
        }

        return resolvedConfig.pointer.element
    }

    // endregion

    // region Script Expression Related

    /**
     * @see ParadoxScriptExpressionSupport.annotate
     */
    fun annotateScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression is empty
        val configExpression = config.configExpression ?: return
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.annotate(element, rangeInElement, text, config, holder)
        }
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolve
     */
    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression is empty
        val configExpression = config.configExpression ?: return null
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.resolve(element, rangeInElement, text, config, role)?.let { return it }
        }
        if (configExpression.role.isKey()) {
            return getResolvedConfigElement(element, config, config.configGroup)
        }
        return null
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolveAll
     */
    fun resolveAllScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression ?: return emptyList()
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.resolveAll(element, rangeInElement, text, config, role).orNull()?.let { return it }
        }
        if (configExpression.role.isKey()) {
            return getResolvedConfigElement(element, config, config.configGroup).to.singletonListOrEmpty()
        }
        return emptyList()
    }

    /**
     * @see ParadoxScriptExpressionSupport.getReferences
     */
    fun getScriptExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression ?: return emptyList()
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.getReferences(element, rangeInElement, text, config, role).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxScriptExpressionSupport.complete
     */
    fun completeScriptExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.complete(context, result)
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
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ ep ->
            if (!ep.supports(element)) return@f // 3.0.1 still check here
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.annotate(element, rangeInElement, text, holder)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolve
     */
    fun resolveLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression is empty
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ ep ->
            if (!ep.supports(element)) return@f // 3.0.1 still check here
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.resolve(element, rangeInElement, text)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolveAll
     */
    fun resolveAllLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ ep ->
            if (!ep.supports(element)) return@f // 3.0.1 still check here
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.resolveAll(element, rangeInElement, text).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.getReferences
     */
    fun getLocalisationExpressionReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String): List<PsiReference> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ ep ->
            if (!ep.supports(element)) return@f // 3.0.1 still check here
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.getReferences(element, rangeInElement, text).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.complete
     */
    fun completeLocalisationExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val configGroup = context.configGroup
        val gameType = configGroup.gameType
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ ep ->
            if (!ep.supports(element)) return@f // 3.0.1 still check here
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
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
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.annotate(element, rangeInElement, text, config, holder)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolve
     */
    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression is empty
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.resolve(element, rangeInElement, text, config)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolveAll
     */
    fun resolveAllCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, text: String, config: CwtValueConfig): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression is empty
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.resolveAll(element, rangeInElement, text, config).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxCsvExpressionSupport.complete
     */
    fun completeCsvExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config?.castOrNull<CwtValueConfig>() ?: return
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check immediately before applying logic
            ep.complete(context, result)
        }
    }

    // endregion
}
