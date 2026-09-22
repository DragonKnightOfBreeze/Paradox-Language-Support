package icu.windea.pls.config.model

import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

/** 并集规则的数据模型。用于保存和获取符合特定条件的并集规则的名字和键。 */
interface CwtUnionModel {
    /** 名字到作为常量的候选项规则的映射。 */
    val forConst: Map<String, Map<@CaseInsensitive String, CwtValueConfig>>
    /** 名字到作为常量以外的一组候选项的映射。 */
    val forNonConstSorted: Map<String, List<CwtValueConfig>>

    companion object {
        @JvmStatic
        fun create(): CwtUnionModelBase = CwtUnionModelBase()

        @JvmStatic
        fun createEmpty(): CwtUnionModel = EmptyCwtUnionModel
    }
}

// region Implementations

class CwtUnionModelBase : CwtUnionModel {
    override val forConst = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtValueConfig>>()
    override val forNonConstSorted = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtValueConfig>>()

    fun trim() {
        forConst.trim()
        forConst.values.forEach { it.trim() }
        forNonConstSorted.trim()
        forNonConstSorted.values.forEach { it.trim() }
    }
}

private object EmptyCwtUnionModel : CwtUnionModel {
    override val forConst: Map<String, Map<@CaseInsensitive String, CwtValueConfig>> get() = emptyMap()
    override val forNonConstSorted: Map<String, List<CwtValueConfig>> get() = emptyMap()
}

// endregion
