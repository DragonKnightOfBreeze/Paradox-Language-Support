package icu.windea.pls.ep.match.expression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptionsService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * 用于匹配 CSV 表达式与规则表达式。
 *
 * 注意：相比 [ParadoxCsvExpressionMatcher]，仅支持有限的 [CwtDataType]。
 *
 * @see ParadoxExpression
 * @see CwtDataExpression
 * @see ParadoxCsvExpressionElement
 * @see ParadoxMatchOptionsService
 * @see ParadoxExpressionMatchService
 */
interface ParadoxCsvExpressionMatcher {
    fun supports(dataType: CwtDataType): Boolean

    /**
     * 匹配 CSV 表达式和规则表达式。
     */
    fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult?

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
            val result = mutableMapOf<CwtDataType, List<ParadoxCsvExpressionMatcher>>()
            val eps = EP_NAME.extensionList
            CwtDataType.entries.values.forEach { dataType -> eps.filterFast { ep -> ep.supports(dataType) }.orNull()?.let { result[dataType] = it.optimized() } }
            return result.optimized()
        }

        // endregion
    }
}
