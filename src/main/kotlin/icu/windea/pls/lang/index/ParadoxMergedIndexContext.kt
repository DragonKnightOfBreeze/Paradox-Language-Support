package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.collectReferences
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.csv.ParadoxCsvExpressionPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationExpressionPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

sealed interface ParadoxMergedIndexContext {
    val file: PsiFile
    val fileData: MutableMap<String, List<ParadoxIndexInfo>>
    val expressionElement: ParadoxExpressionElement?
    val expressionReferences: Array<out PsiReference>
}

sealed interface ParadoxMergedIndexScriptContext : ParadoxMergedIndexContext {
    override val file: ParadoxScriptFile
    override val expressionElement: ParadoxScriptStringExpressionElement?
    val configs: List<CwtMemberConfig<*>>
    val definitionCandidateInfo: ParadoxDefinitionCandidateInfo?
    val definitionCandidateAvailableTypes: Set<ParadoxMergedIndexType<*>>
    val definitionCandidateAvailableTypesUnchanged: Boolean
}

/**
 * @see ParadoxExpressionService.resolveScriptExpressionReferences
 */
data class ParadoxMergedIndexScriptContextBase(
    override val file: ParadoxScriptFile,
    override val fileData: MutableMap<String, List<ParadoxIndexInfo>>,
) : ParadoxMergedIndexScriptContext {
    override var expressionElement: ParadoxScriptStringExpressionElement? = null
    override val expressionReferences: Array<out PsiReference> // region by lazy { computeExpressionReferences() }
        get() = LazyValue.of({ _expressionReferences }, { _expressionReferences = it }) { computeExpressionReferences() }
    private var _expressionReferences: Array<out PsiReference>? = null // endregion
    override val configs: List<CwtMemberConfig<*>>  // region by lazy { computeConfigs() }
        get() = LazyValue.of({ _configs }, { _configs = it }) { computeConfigs() }
    private var _configs: List<CwtMemberConfig<*>>? = null // endregion
    override var definitionCandidateInfo: ParadoxDefinitionCandidateInfo? = null
    override var definitionCandidateAvailableTypes: Set<ParadoxMergedIndexType<*>> = emptySet()
    override var definitionCandidateAvailableTypesUnchanged: Boolean = true

    // 3.0.1 thread local cache, so thread safe is unnecessary

    private fun computeExpressionReferences(): Array<out PsiReference> {
        val element = expressionElement ?: return PsiReference.EMPTY_ARRAY
        val configs = configs
        if (configs.isEmpty()) return PsiReference.EMPTY_ARRAY
        val role = ParadoxTypeResolver.resolveExpressionRole(element)
        val referenceRange = ParadoxExpressionService.getExpressionRangeInElement(element)
        if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxScriptExpressionPsiReference(element, referenceRange, configs, role)
        return reference.collectReferences()
    }

    private fun computeConfigs(): List<CwtMemberConfig<*>> {
        // 3.0.l it's safe to call `ParadoxConfigService.getConfigs` directly during indexing (with dumb mode)
        val element = expressionElement ?: return emptyList()
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        val isKey = element is ParadoxScriptPropertyKey
        val isDumb = ParadoxMatchService.isDumb()
        val options = if (isDumb) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val configs = ParadoxConfigService.getConfigs(memberElement, options.copy(fallback = isKey))
        return configs
    }

    fun resetCache() {
        _expressionReferences = null
        _configs = null
    }
}

sealed interface ParadoxMergedIndexLocalisationContext : ParadoxMergedIndexContext {
    override val file: ParadoxLocalisationFile
    override val expressionElement: ParadoxLocalisationExpressionElement?
    val localisation: ParadoxLocalisationProperty?
}

/**
 * @see ParadoxExpressionService.resolveLocalisationExpressionReferences
 */
data class ParadoxMergedIndexLocalisationContextBase(
    override val file: ParadoxLocalisationFile,
    override val fileData: MutableMap<String, List<ParadoxIndexInfo>>,
) : ParadoxMergedIndexLocalisationContext {
    override var expressionElement: ParadoxLocalisationExpressionElement? = null
    override val expressionReferences: Array<out PsiReference> // region by lazy { computeExpressionReferences() }
        get() = LazyValue.of({ _expressionReferences }, { _expressionReferences = it }) { computeExpressionReferences() }
    private var _expressionReferences: Array<out PsiReference>? = null // endregion
    override var localisation: ParadoxLocalisationProperty? = null

    // 3.0.1 thread local cache, so thread safe is unnecessary

    private fun computeExpressionReferences(): Array<out PsiReference> {
        val element = expressionElement ?: return PsiReference.EMPTY_ARRAY
        val referenceRange = ParadoxExpressionService.getExpressionRangeInElement(element)
        if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxLocalisationExpressionPsiReference(element, referenceRange)
        return reference.collectReferences()
    }

    fun resetCache() {
        _expressionReferences = null
    }
}

sealed interface ParadoxMergedIndexCsvContext : ParadoxMergedIndexContext {
    override val file: ParadoxCsvFile
    override val expressionElement: ParadoxCsvExpressionElement?
    val columnConfig: CwtPropertyConfig?
}

/**
 * @see ParadoxExpressionService.resolveCsvExpressionReferences
 */
data class ParadoxMergedIndexCsvContextBase(
    override val file: ParadoxCsvFile,
    override val fileData: MutableMap<String, List<ParadoxIndexInfo>>,
) : ParadoxMergedIndexCsvContext {
    override var expressionElement: ParadoxCsvExpressionElement? = null
    override val expressionReferences: Array<out PsiReference> // region by lazy { computeExpressionReferences() }
        get() = LazyValue.of({ _expressionReferences }, { _expressionReferences = it }) { computeExpressionReferences() }
    private var _expressionReferences: Array<out PsiReference>? = null // endregion
    override val columnConfig: CwtPropertyConfig? // region by lazy { computeColumnConfig() }
        get() = LazyValue.ofNullable({ _columnConfig }, { _columnConfig = it }) { computeColumnConfig() }
    private var _columnConfig = LazyValue.UNINITIALIZED // endregion

    // 3.0.1 thread local cache, so thread safe is unnecessary

    private fun computeExpressionReferences(): Array<out PsiReference> {
        val element = expressionElement ?: return PsiReference.EMPTY_ARRAY
        val columnConfig = columnConfig ?: return PsiReference.EMPTY_ARRAY
        val referenceRange = ParadoxExpressionService.getExpressionRangeInElement(element)
        if (referenceRange.isEmpty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxCsvExpressionPsiReference(element, referenceRange, columnConfig)
        return arrayOf(reference)
    }

    private fun computeColumnConfig(): CwtPropertyConfig? {
        val element = expressionElement ?: return null
        if (element !is ParadoxCsvColumn) return null
        val columnConfig = ParadoxConfigManager.getColumnConfig(element)
        return columnConfig
    }

    fun resetCache() {
        _expressionReferences = null
        _columnConfig = LazyValue.UNINITIALIZED
    }
}
