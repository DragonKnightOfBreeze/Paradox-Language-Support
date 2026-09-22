package icu.windea.pls.config.model

import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

/**
 * 链接规则的数据模型。
 *
 * 用于保存和获取符合特定条件的链接规则。
 */
interface CwtLinkModel {
    /** 变量对应的链接规则的列表。 */
    val variable: List<CwtLinkConfig>
    val forScopeStatic: List<CwtLinkConfig>
    val forScopeNoPrefixSorted: List<CwtLinkConfig>
    val forScopeFromDataSorted: List<CwtLinkConfig>
    val forScopeFromArgumentSorted: List<CwtLinkConfig>
    val forScopeFromArgumentSortedByPrefix: Map<@CaseInsensitive String, List<CwtLinkConfig>>
    val forValueStatic: List<CwtLinkConfig>
    val forValueNoPrefixSorted: List<CwtLinkConfig>
    val forValueFromDataSorted: List<CwtLinkConfig>
    val forValueFromArgumentSorted: List<CwtLinkConfig>
    val forValueFromArgumentSortedByPrefix: Map<@CaseInsensitive String, List<CwtLinkConfig>>

    companion object {
        @JvmStatic
        fun create(): CwtLinkModelBase = CwtLinkModelBase()

        @JvmStatic
        fun createEmpty(): CwtLinkModel = EmptyCwtLinkModel
    }
}

// region Implementations

class CwtLinkModelBase : CwtLinkModel {
    override val variable = ObjectArrayList<CwtLinkConfig>()
    override val forScopeStatic = ObjectArrayList<CwtLinkConfig>()
    override val forScopeNoPrefixSorted = ObjectArrayList<CwtLinkConfig>()
    override val forScopeFromDataSorted = ObjectArrayList<CwtLinkConfig>()
    override val forScopeFromArgumentSorted = ObjectArrayList<CwtLinkConfig>()
    override val forScopeFromArgumentSortedByPrefix = CaseInsensitiveStringKeyMap<ObjectArrayList<CwtLinkConfig>>()
    override val forValueStatic = ObjectArrayList<CwtLinkConfig>()
    override val forValueNoPrefixSorted = ObjectArrayList<CwtLinkConfig>()
    override val forValueFromDataSorted = ObjectArrayList<CwtLinkConfig>()
    override val forValueFromArgumentSorted = ObjectArrayList<CwtLinkConfig>()
    override val forValueFromArgumentSortedByPrefix = CaseInsensitiveStringKeyMap<ObjectArrayList<CwtLinkConfig>>()

    fun trim() {
        variable.trim()
        forScopeStatic.trim()
        forScopeNoPrefixSorted.trim()
        forScopeFromDataSorted.trim()
        forScopeFromArgumentSorted.trim()
        forScopeFromArgumentSortedByPrefix.trim()
        forScopeFromArgumentSortedByPrefix.values.forEach { it.trim() }
        forValueStatic.trim()
        forValueNoPrefixSorted.trim()
        forValueFromDataSorted.trim()
        forValueFromArgumentSorted.trim()
        forValueFromArgumentSortedByPrefix.trim()
        forValueFromArgumentSortedByPrefix.values.forEach { it.trim() }
    }
}

private object EmptyCwtLinkModel : CwtLinkModel {
    override val variable: List<CwtLinkConfig> get() = emptyList()
    override val forScopeStatic: List<CwtLinkConfig> get() = emptyList()
    override val forScopeNoPrefixSorted: List<CwtLinkConfig> get() = emptyList()
    override val forScopeFromDataSorted: List<CwtLinkConfig> get() = emptyList()
    override val forScopeFromArgumentSorted: List<CwtLinkConfig> get() = emptyList()
    override val forScopeFromArgumentSortedByPrefix: Map<@CaseInsensitive String, List<CwtLinkConfig>> get() = emptyMap()
    override val forValueStatic: List<CwtLinkConfig> get() = emptyList()
    override val forValueNoPrefixSorted: List<CwtLinkConfig> get() = emptyList()
    override val forValueFromDataSorted: List<CwtLinkConfig> get() = emptyList()
    override val forValueFromArgumentSorted: List<CwtLinkConfig> get() = emptyList()
    override val forValueFromArgumentSortedByPrefix: Map<@CaseInsensitive String, List<CwtLinkConfig>> get() = emptyMap()
}

// endregion
