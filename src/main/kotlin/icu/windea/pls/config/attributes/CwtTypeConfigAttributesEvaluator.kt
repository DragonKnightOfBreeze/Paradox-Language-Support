package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.ReversibleValue

/**
 * 类型规则的综合属性的评估器。
 *
 * @see CwtTypeConfig
 * @see CwtTypeConfigAttributes
 */
object CwtTypeConfigAttributesEvaluator {
    private data class Context(
        val involvedTypeKeys: MutableSet<@CaseInsensitive String> = caseInsensitiveStringSet(),
        val possibleTypeKeys: MutableSet<@CaseInsensitive String> = caseInsensitiveStringSet(),
    )

    fun evaluate(config: CwtTypeConfig): CwtTypeConfigAttributes {
        val context = Context()
        processTypeKeyFilter(context, config.typeKeyFilter)
        config.subtypes.values.forEach { subtypeConfig ->
            processTypeKeyFilter(context, subtypeConfig.typeKeyFilter)
        }
        return buildAttributes(context)
    }

    private fun processTypeKeyFilter(context: Context, typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?) {
        val (v, o) = typeKeyFilter ?: return
        context.involvedTypeKeys.addAll(v)
        if (o) context.possibleTypeKeys.addAll(v)
    }

    private fun buildAttributes(context: Context): CwtTypeConfigAttributes {
        val result = CwtTypeConfigAttributes(
            context.involvedTypeKeys.optimized(),
            context.possibleTypeKeys.optimized(),
        )
        if (result == CwtTypeConfigAttributes.EMPTY) return CwtTypeConfigAttributes.EMPTY
        return result
    }
}
