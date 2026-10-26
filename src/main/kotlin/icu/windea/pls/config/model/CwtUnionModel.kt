package icu.windea.pls.config.model

import icu.windea.pls.config.config.detached.CwtExpandedUnionConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

/** 并集规则的数据模型。用于保存和获取符合特定条件的并集规则的名字和键。 */
interface CwtUnionModel {
    /** 名字到展开后的所有并集规则的映射。按原始声明顺序排列。 */
    val expanded: Map<String, List<CwtExpandedUnionConfig>>
    /** 名字到展开后的一组常量型的并集规则的映射。 */
    val expandedForConst: Map<String, Map<@CaseInsensitive String, CwtExpandedUnionConfig>>
    /** 名字到展开后的一组常量型以外的并集规则的映射。按数据类型的优先级排序。 */
    val expandedForNonConst: Map<String, List<CwtExpandedUnionConfig>>
    /** 名字到作为常量的规则的数量的映射。 */
    val countForConst: Map<String, Int>
    /** 名字到作为常量以外的规则的数量的映射。 */
    val countForNonConst: Map<String, Int>

    companion object {
        @JvmStatic
        fun create(): CwtUnionModelBase = CwtUnionModelBase()

        @JvmStatic
        fun createEmpty(): CwtUnionModel = EmptyCwtUnionModel
    }
}

// region Implementations

class CwtUnionModelBase : CwtUnionModel {
    override val expanded = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExpandedUnionConfig>>()
    override val expandedForConst = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtExpandedUnionConfig>>()
    override val expandedForNonConst = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExpandedUnionConfig>>()
    override val countForConst = Object2IntOpenHashMap<String>()
    override val countForNonConst = Object2IntOpenHashMap<String>()

    fun trim() {
        expanded.trim()
        expanded.values.forEach { it.trim() }
        expandedForConst.trim()
        expandedForConst.values.forEach { it.trim() }
        expandedForNonConst.trim()
        countForConst.trim()
        countForNonConst.trim()
    }
}

private object EmptyCwtUnionModel : CwtUnionModel {
    override val expanded: Map<String, List<CwtExpandedUnionConfig>> get() = emptyMap()
    override val expandedForConst: Map<String, Map<@CaseInsensitive String, CwtExpandedUnionConfig>> get() = emptyMap()
    override val expandedForNonConst: Map<String, List<CwtExpandedUnionConfig>> get() = emptyMap()
    override val countForConst: Map<String, Int> get() = emptyMap()
    override val countForNonConst: Map<String, Int> get() = emptyMap()
}

// endregion
