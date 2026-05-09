package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.annotations.CaseInsensitive

/**
 * 类型规则的综合属性。
 *
 * 用于进行更准确的语义解析与匹配。
 *
 * @property involvedTypeKeys 涉及到的类型键的集合。
 * @property possibleTypeKeys 可能的类型键的集合。
 *
 * @see CwtTypeConfig
 * @see CwtTypeConfigAttributesEvaluator
 */
data class CwtTypeConfigAttributes(
    val involvedTypeKeys: Set<@CaseInsensitive String> = emptySet(),
    val possibleTypeKeys: Set<@CaseInsensitive String> = emptySet(),
) : CwtConfigAttributes {
    companion object {
        @JvmField val EMPTY = CwtTypeConfigAttributes()
    }
}
