package icu.windea.pls.config.model

import icu.windea.pls.core.annotations.CaseInsensitive
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

/**
 * 别名规则的数据模型。
 *
 * 用于保存和获取符合特定条件的别名规则的名字和键。
 */
interface CwtAliasModel {
    /** 名字到作为常量的键的映射。 */
    val forConst: Map<String, Map<@CaseInsensitive String, String>>
    /** 名字到作为常量以外的一组键的映射。 */
    val forNonConstSorted: Map<String, Set<String>>
    /** 名字到作为常量的规则的数量的映射。 */
    val configCountForConst: Map<String, Int>
    /** 名字到作为常量以外的规则的数量的映射。 */
    val configCountForNonConst: Map<String, Int>
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
    override val forConst = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, String>>()
    override val forNonConstSorted = Object2ObjectLinkedOpenHashMap<String, ObjectLinkedOpenHashSet<String>>()
    override val configCountForConst = Object2IntOpenHashMap<String>()
    override val configCountForNonConst = Object2IntOpenHashMap<String>()
    override val supportScope = ObjectLinkedOpenHashSet<String>()

    fun trim() {
        forConst.trim()
        forConst.values.forEach { it.trim() }
        forNonConstSorted.trim()
        forNonConstSorted.values.forEach { it.trim() }
        supportScope.trim()
    }
}

private object EmptyCwtAliasModel : CwtAliasModel {
    override val forConst: Map<String, Map<@CaseInsensitive String, String>> get() = emptyMap()
    override val forNonConstSorted: Map<String, Set<String>> get() = emptyMap()
    override val configCountForConst: Map<String, Int> get() = emptyMap()
    override val configCountForNonConst: Map<String, Int> get() = emptyMap()
    override val supportScope: Set<String> get() = emptySet()
}

// endregion
