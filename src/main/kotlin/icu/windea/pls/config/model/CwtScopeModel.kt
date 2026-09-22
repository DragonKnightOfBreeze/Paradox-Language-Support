package icu.windea.pls.config.model

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArraySet

/**
 * 作用域的数据模型。
 *
 * 用于保存和获取作用域的关系信息（别名、继承等），以优化匹配和合并作用域时的性能。
 */
interface CwtScopeModel {
    /** 基础作用域到其别名形式的映射（仅保存索引计数，不包括直接匹配的情况）。 */
    val base2Aliases: Map<Int, Set<Int>>
    /** 基础作用域到其父作用域的映射（仅保存索引计数，兼容别名形式，不包括直接匹配的情况）。 */
    val base2ParentScopes: Map<Int, Set<Int>>
    /** 基础作用域到其子作用域的映射（仅保存索引计数，兼容别名形式，不包括直接匹配的情况）。 */
    val base2ChildScopes: Map<Int, Set<Int>>
    /** 基础作用域到匹配的作用域的映射（仅保存索引计数，兼容别名形式，不包括直接匹配的情况）。 */
    val base2MatchedScopes: Map<Int, Set<Int>>
    /** 基础作用域到提升后的作用域的映射（仅保存索引计数，兼容别名形式，不包括直接匹配的情况）。 */
    val base2PromotedScopes: Map<Int, Set<Int>>

    companion object {
        @JvmStatic
        fun create(): CwtScopeModelBase = CwtScopeModelBase()

        @JvmStatic
        fun createEmpty(): CwtScopeModel = EmptyCwtScopeModel
    }
}

// region Implementations

class CwtScopeModelBase : CwtScopeModel {
    override val base2Aliases = Int2ObjectOpenHashMap<IntArraySet>()
    override val base2ParentScopes = Int2ObjectOpenHashMap<IntArraySet>()
    override val base2ChildScopes = Int2ObjectOpenHashMap<IntArraySet>()
    override val base2MatchedScopes = Int2ObjectOpenHashMap<IntArraySet>()
    override val base2PromotedScopes = Int2ObjectOpenHashMap<IntArraySet>()

    fun trim() {
        base2Aliases.trim()
        base2ParentScopes.trim()
        base2ChildScopes.trim()
        base2MatchedScopes.trim()
        base2PromotedScopes.trim()
    }
}

private object EmptyCwtScopeModel : CwtScopeModel {
    override val base2Aliases: Map<Int, Set<Int>> get() = emptyMap()
    override val base2ParentScopes: Map<Int, Set<Int>> get() = emptyMap()
    override val base2ChildScopes: Map<Int, Set<Int>> get() = emptyMap()
    override val base2MatchedScopes: Map<Int, Set<Int>> get() = emptyMap()
    override val base2PromotedScopes: Map<Int, Set<Int>> get() = emptyMap()
}

// endregion
