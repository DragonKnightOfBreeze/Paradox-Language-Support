package icu.windea.pls.ep.match.expression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * 用于匹配 CSV 表达式与规则表达式。
 *
 * 注意：相比 [ParadoxCsvExpressionMatcher]，仅支持有限的 [CwtDataType]。
 *
 * @see ParadoxExpression
 * @see CwtDataExpression
 * @see ParadoxCsvExpressionElement
 * @see ParadoxMatchService
 * @see ParadoxExpressionMatchService
 */
interface ParadoxCsvExpressionMatcher {
    /**
     * 匹配 CSV 表达式和规则表达式。
     */
    fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxCsvExpressionMatcher>("icu.windea.pls.csvExpressionMatcher")
        @JvmField val CACHE = LazyValue<Map<CwtDataType, List<ParadoxCsvExpressionMatcher>>>()

        @JvmStatic
        fun getAll(dataType: CwtDataType): List<ParadoxCsvExpressionMatcher> = CACHE.get()?.get(dataType).orEmpty()

        // region Implementations

        init {
            CACHE.initialize { computeCache() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { computeCache() } }
        }

        private fun computeCache(): Map<CwtDataType, List<ParadoxCsvExpressionMatcher>> {
            val result = mutableMapOf<CwtDataType, MutableList<ParadoxCsvExpressionMatcher>>()
            val eps = EP_NAME.extensionList
            eps.forEachFast { ep ->
                when (ep) {
                    is ParadoxCsvCompositeExpressionMatcher -> {
                        val matchers = ep.matcherMap
                        matchers.forEach { (matcher, dataTypes) ->
                            dataTypes.forEach { dataType ->
                                result.computeIfAbsent(dataType) { mutableListOf() } += matcher
                            }
                        }
                    }
                    is ParadoxCsvSimpleExpressionMatcher -> {
                        ep.dataTypes.forEach { dataType ->
                            result.computeIfAbsent(dataType) { mutableListOf() } += ep
                        }
                    }
                    else -> {
                        // fallback
                        CwtDataType.entries.values.forEach { dataType ->
                            result.computeIfAbsent(dataType) { mutableListOf() } += ep
                        }
                    }
                }
            }
            return result.mapValues { (_, v) -> v.optimized() }.optimized()
        }

        // endregion
    }
}
