package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.lang.index.ParadoxMergedIndex

/**
 * 行规则的综合属性。
 *
 * 用于进行更准确的语义解析与匹配，以及优化构建合并索引（[ParadoxMergedIndex]）时的性能。
 *
 * @see CwtRowConfig
 * @see CwtRowConfigAttributesEvaluator
 */
data class CwtRowConfigAttributes(
    val involvesDynamicValue: Boolean = false,
) {
    companion object {
        @JvmField val EMPTY = CwtRowConfigAttributes()
    }
}
