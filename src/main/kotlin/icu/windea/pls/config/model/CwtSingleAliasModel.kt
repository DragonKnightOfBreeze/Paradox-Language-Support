package icu.windea.pls.config.model

import icu.windea.pls.config.config.detached.CwtExpandedSingleAliasConfig
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap

/**
 * 单别名规则的数据模型。
 *
 * 用于保存和获取展开后的单别名规则。
 */
interface CwtSingleAliasModel {
    /** 名字到展开后的单别名规则的映射。 */
    val expanded: Map<String, CwtExpandedSingleAliasConfig>

    companion object {
        @JvmStatic
        fun create(): CwtSingleAliasModelBase = CwtSingleAliasModelBase()

        @JvmStatic
        fun createEmpty(): CwtSingleAliasModel = EmptyCwtSingleAliasModel
    }
}

// region Implementations

class CwtSingleAliasModelBase : CwtSingleAliasModel {
    override val expanded = Object2ObjectLinkedOpenHashMap<String, CwtExpandedSingleAliasConfig>()

    fun trim() {
        expanded.trim()
    }
}

private object EmptyCwtSingleAliasModel : CwtSingleAliasModel {
    override val expanded: Map<String, CwtExpandedSingleAliasConfig> get() = emptyMap()
}

// endregion
