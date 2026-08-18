package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collectReferences
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isEmpty
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.processChild
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.CwtMemberConfigLightElement
import icu.windea.pls.lang.references.ParadoxComplexEnumValuePsiReference
import icu.windea.pls.lang.references.csv.ParadoxCsvExpressionPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationExpressionPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.model.orSpecific
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@Optimized
object ParadoxExpressionService {
    // region Common Methods

    /**
     * 得到 [element] 的用于语义解析的表达式文本。
     */
    fun getExpressionText(element: ParadoxExpressionElement, rangeInElement: TextRange? = null): String {
        return when (element) {
            is ParadoxScriptBlock -> "" // should not be used
            is ParadoxScriptInlineMath -> "" // should not be used
            is ParadoxScriptStringExpressionElement, is ParadoxCsvColumn -> rangeInElement?.substring(element.text) ?: element.value
            else -> rangeInElement?.substring(element.text) ?: element.text
        }
    }

    /**
     * 得到 [element] 的用于语义解析的表达式文本范围。相对于完整的表达式文本。
     */
    fun getExpressionRangeInExpression(element: ParadoxExpressionElement, rangeInElement: TextRange? = null): TextRange {
        return when (element) {
            is ParadoxScriptBlock -> TextRange.EMPTY_RANGE // should not be used
            is ParadoxScriptInlineMath -> TextRange.EMPTY_RANGE // should not be used
            is ParadoxScriptStringExpressionElement, is ParadoxCsvColumn -> {
                if (rangeInElement == null) return TextRange.create(0, element.value.length)
                val text = element.text
                if (text.isEmpty()) return TextRange.EMPTY_RANGE
                val startOffset = if (text.isLeftQuoted() && rangeInElement.startOffset <= 0) 1 else 0
                val endOffset = if (text.isRightQuoted() && rangeInElement.endOffset >= text.length) 1 else 0
                TextRange.create(startOffset, text.length - endOffset)
            }
            else -> rangeInElement ?: TextRange.create(0, element.textLength)
        }
    }

    /**
     * 得到 [element] 的用于语义解析的表达式文本范围。相对于表达式 PSI 元素。
     */
    fun getExpressionRangeInElement(element: ParadoxExpressionElement): TextRange {
        return when (element) {
            is ParadoxScriptBlock -> TextRange.create(0, 1) // `{`
            is ParadoxScriptInlineMath -> element.firstChild.textRangeInParent // `@[` or `@\[`
            is ParadoxScriptStringExpressionElement, is ParadoxCsvColumn -> {
                val text = element.text
                if (text.isEmpty()) return TextRange.EMPTY_RANGE
                val startOffset = if (text.isLeftQuoted()) 1 else 0
                val endOffset = if (text.isRightQuoted()) 1 else 0
                TextRange.create(startOffset, text.length - endOffset)
            }
            else -> TextRange.create(0, element.textLength)
        }
    }

    /**
     * 得到 [element] 的用于语义解析的表达式文本的开始偏移（相对于 [element]）。
     */
    fun getExpressionOffset(element: ParadoxExpressionElement): Int {
        return when (element) {
            is ParadoxScriptStringExpressionElement, is ParadoxCsvColumn -> if (element.text.isLeftQuoted()) 1 else 0
            else -> 0
        }
    }

    fun getParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
        // NOTE 3.0.1 不要缓存，因为自身的计算逻辑已经足够块
        var parameterRanges: MutableList<TextRange>? = null
        element.processChild { e ->
            if (isParameterElementInExpression(e)) {
                if (parameterRanges == null) parameterRanges = mutableListOf()
                parameterRanges.add(e.textRange)
            }
            true
        }
        return parameterRanges.orEmpty()
    }

    fun isParameterElementInExpression(element: PsiElement): Boolean {
        return element is ParadoxParameter || element is ParadoxScriptInlineConditionalBlock || element is ParadoxLocalisationParameter
    }

    // endregion

    // region Annotate Methods

    /**
     * @see ParadoxScriptExpressionSupport.annotate
     */
    fun annotateScriptExpression(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression text is empty
        if (rangeInExpression.isEmpty) return
        val configExpression = config.configExpression ?: return
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.annotate(element, text, rangeInExpression, config, holder)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.annotate
     */
    fun annotateLocalisationExpression(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression text is empty
        if (rangeInExpression.isEmpty) return
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.annotate(element, text, rangeInExpression, holder)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.annotate
     */
    fun annotateCsvExpression(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig, holder: AnnotationHolder) {
        if (text.isEmpty()) return // skip if expression text is empty
        if (rangeInExpression.isEmpty) return
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.getAll(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.annotate(element, text, rangeInExpression, config, holder)
        }
    }

    // endregion

    // region Resolve Methods

    /**
     * @see ParadoxScriptExpressionSupport.resolve
     */
    fun resolveScriptExpression(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression text is empty
        if (text.isParameterized()) return null // ignore if expression text is parameterized
        val configExpression = config.configExpression ?: return null
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.resolve(element, text, rangeInExpression, config, role)?.let { return it }
        }
        if (configExpression.role.isKey()) {
            return getResolvedConfigElement(element, config, config.configGroup)
        }
        return null
    }

    /**
     * @see ParadoxScriptExpressionSupport.resolveAll
     */
    fun resolveAllScriptExpression(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression text is empty
        if (text.isParameterized()) return emptyList() // ignore if expression text is parameterized
        val configExpression = config.configExpression ?: return emptyList()
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.resolveAll(element, text, rangeInExpression, config, role).orNull()?.let { return it }
        }
        if (configExpression.role.isKey()) {
            return getResolvedConfigElement(element, config, config.configGroup).to.singletonListOrEmpty()
        }
        return emptyList()
    }

    /**
     * @see ParadoxScriptExpressionSupport.getReferences
     */
    fun getScriptExpressionReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
        if (text.isEmpty()) return emptyList() // ignore if expression text is empty
        val configExpression = config.configExpression ?: return emptyList()
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.getReferences(element, text, rangeInExpression, config, role).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolve
     */
    fun resolveLocalisationExpression(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression text is empty
        if (text.isParameterized()) return null // ignore if expression text is parameterized
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            if (!support.supports(element)) return@f // 3.0.1 still check here
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.resolve(element, text, rangeInExpression)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.resolveAll
     */
    fun resolveAllLocalisationExpression(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression text is empty
        if (text.isParameterized()) return emptyList() // ignore if expression text is parameterized
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            if (!support.supports(element)) return@f // 3.0.1 still check here
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.resolveAll(element, text, rangeInExpression).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.getReferences
     */
    fun getLocalisationExpressionReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange): List<PsiReference> {
        if (text.isEmpty()) return emptyList() // ignore if expression text is empty
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            if (!support.supports(element)) return@f // 3.0.1 still check here
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.getReferences(element, text, rangeInExpression).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolve
     */
    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): PsiElement? {
        if (text.isEmpty()) return null // ignore if expression text is empty
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.getAll(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.resolve(element, text, rangeInExpression, config)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxCsvExpressionSupport.resolveAll
     */
    fun resolveAllCsvExpression(element: ParadoxCsvExpressionElement, text: String, rangeInExpression: TextRange, config: CwtValueConfig): List<PsiElement> {
        if (text.isEmpty()) return emptyList() // ignore if expression text is empty
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.getAll(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.resolveAll(element, text, rangeInExpression, config).orNull()?.let { return it }
        }
        return emptyList()
    }

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

    // region PSI Reference Methods

    fun resolveScriptExpressionReferences(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        // 尝试解析为复杂枚举值声明
        run {
            if (element is ParadoxScriptBlock) return@run
            val complexEnumValueInfo = ParadoxComplexEnumValueManager.getInfo(element) ?: return@run
            val referenceRange = getExpressionRangeInElement(element)
            if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
            val reference = ParadoxComplexEnumValuePsiReference(element, referenceRange, complexEnumValueInfo)
            return arrayOf(reference)
        }

        // 尝试基于规则进行解析
        val isKey = element is ParadoxScriptPropertyKey
        val isDumb = ParadoxMatchService.isDumb()
        val options = if (isDumb) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val configs = ParadoxConfigManager.getConfigs(element, options.copy(fallback = isKey))
        if (configs.isEmpty()) return PsiReference.EMPTY_ARRAY
        val role = ParadoxTypeResolver.resolveExpressionRole(element)
        val referenceRange = getExpressionRangeInElement(element)
        if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxScriptExpressionPsiReference(element, referenceRange, configs, role)
        return reference.collectReferences()
    }

    fun resolveLocalisationExpressionReferences(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        // 尝试解析为复杂表达式
        val referenceRange = getExpressionRangeInElement(element)
        if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxLocalisationExpressionPsiReference(element, referenceRange)
        return reference.collectReferences()
    }

    fun resolveCsvExpressionReferences(element: ParadoxCsvExpressionElement): Array<out PsiReference> {
        // 尝试解析为复杂枚举值声明
        run {
            val complexEnumValueInfo = ParadoxComplexEnumValueManager.getInfo(element) ?: return@run
            val referenceRange = getExpressionRangeInElement(element)
            if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
            val reference = ParadoxComplexEnumValuePsiReference(element, referenceRange, complexEnumValueInfo)
            return arrayOf(reference)
        }

        // 尝试基于规则进行解析
        if (element !is ParadoxCsvColumn) return PsiReference.EMPTY_ARRAY
        val columnConfig = ParadoxCsvManager.getColumnConfig(element)
        if (columnConfig == null) return PsiReference.EMPTY_ARRAY
        val referenceRange = getExpressionRangeInElement(element)
        if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxCsvExpressionPsiReference(element, referenceRange, columnConfig)
        return arrayOf(reference)
    }

    // endregion

    // region Complete Methods

    /**
     * @see ParadoxScriptExpressionSupport.complete
     */
    fun completeScriptExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config ?: return
        val configExpression = config.configExpression ?: return
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxScriptExpressionSupport.get(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.complete(context, result)
        }
    }

    /**
     * @see ParadoxLocalisationExpressionSupport.complete
     */
    fun completeLocalisationExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val configGroup = context.configGroup
        val gameType = configGroup.gameType
        val supports = ParadoxLocalisationExpressionSupport.getAll() // 3.0.1 use global cache (all supports)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            if (!support.supports(element)) return@f // 3.0.1 still check here
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.complete(context, result)
        }
    }

    /**
     * @see ParadoxCsvExpressionSupport.complete
     */
    fun completeCsvExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val config = context.config?.castOrNull<CwtValueConfig>() ?: return
        val configExpression = config.configExpression
        val dataType = configExpression.type
        val gameType = config.configGroup.gameType
        val supports = ParadoxCsvExpressionSupport.getAll(dataType) // 3.0.1 optimize: use global cache (by data type)
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check canceled immediately before applying logic
            support.complete(context, result)
        }
    }

    // endregion
}
