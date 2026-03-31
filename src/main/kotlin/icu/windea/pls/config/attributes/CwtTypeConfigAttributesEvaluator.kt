package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.ReversibleValue

/**
 * 类型规则的综合属性的评估器。
 *
 * @see CwtTypeConfig
 * @see CwtTypeConfigAttributes
 */
@Optimized
class CwtTypeConfigAttributesEvaluator {
    private val involvedTypeKeys = caseInsensitiveStringSet()
    private val possibleTypeKeys = caseInsensitiveStringSet()

    fun evaluate(config: CwtTypeConfig): CwtTypeConfigAttributes {
        processTypeKeyFilter(config.typeKeyFilter)
        config.subtypes.values.forEach { subtypeConfig ->
            processTypeKeyFilter(subtypeConfig.typeKeyFilter)
        }
        return buildAttributes()
    }

    private fun processTypeKeyFilter(typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?) {
        val (v, o) = typeKeyFilter ?: return
        involvedTypeKeys.addAll(v)
        if (o) possibleTypeKeys.addAll(v)
    }

    private fun buildAttributes(): CwtTypeConfigAttributes {
        val result = CwtTypeConfigAttributes(
            involvedTypeKeys.optimized(),
            possibleTypeKeys.optimized(),
        )
        if (result == CwtTypeConfigAttributes.EMPTY) return CwtTypeConfigAttributes.EMPTY
        return result
    }
}
