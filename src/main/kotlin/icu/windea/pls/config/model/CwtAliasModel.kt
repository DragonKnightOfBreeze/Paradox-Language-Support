package icu.windea.pls.config.model

import icu.windea.pls.config.config.detached.CwtExpandedAliasConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

/**
 * 别名规则的数据模型。
 *
 * 用于保存和获取符合特定条件的别名规则的名字和键。
 */
interface CwtAliasModel {
    /** 名字到展开后的所有别名规则的映射。按原始声明顺序排列。 */
    val expanded: Map<String, List<CwtExpandedAliasConfig>>
    /** 名字到展开后的一组常量型的别名规则的映射。 */
    val expandedForConst: Map<String, Map<@CaseInsensitive String, CwtExpandedAliasConfig>>
    /** 名字到展开后的一组常量型以外的别名规则的映射。按数据类型的优先级排序。 */
    val expandedForNonConst: Map<String, List<CwtExpandedAliasConfig>>
    /** 名字到作为常量的规则的数量的映射。 */
    val countForConst: Map<String, Int>
    /** 名字到作为常量以外的规则的数量的映射。 */
    val countForNonConst: Map<String, Int>
    /** （必定）支持作用域的别名规则的名字。 */
    val supportScope: Set<String>

    companion object {
        @JvmStatic
        fun create(): CwtAliasModelBase = CwtAliasModelBase()

        @JvmStatic
        fun createEmpty(): CwtAliasModel = EmptyCwtAliasModel
    }
}

// region Implementations

class CwtAliasModelBase : CwtAliasModel {
    override val expanded = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExpandedAliasConfig>>()
    override val expandedForConst = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtExpandedAliasConfig>>()
    override val expandedForNonConst = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExpandedAliasConfig>>()
    override val countForConst = Object2IntOpenHashMap<String>()
    override val countForNonConst = Object2IntOpenHashMap<String>()
    override val supportScope = ObjectLinkedOpenHashSet<String>()

    fun trim() {
        expanded.trim()
        expanded.values.forEach { it.trim() }
        expandedForConst.trim()
        expandedForConst.values.forEach { it.trim() }
        expandedForNonConst.trim()
        expandedForNonConst.values.forEach { it.trim() }
        countForConst.trim()
        countForNonConst.trim()
        supportScope.trim()
    }
}

private object EmptyCwtAliasModel : CwtAliasModel {
    override val expanded: Map<String, List<CwtExpandedAliasConfig>> get() = emptyMap()
    override val expandedForConst: Map<String, Map<@CaseInsensitive String, CwtExpandedAliasConfig>> get() = emptyMap()
    override val expandedForNonConst: Map<String, List<CwtExpandedAliasConfig>> get() = emptyMap()
    override val countForConst: Map<String, Int> get() = emptyMap()
    override val countForNonConst: Map<String, Int> get() = emptyMap()
    override val supportScope: Set<String> get() = emptySet()
}

// endregion
