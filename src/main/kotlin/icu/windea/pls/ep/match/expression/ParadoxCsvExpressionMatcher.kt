package icu.windea.pls.ep.match.expression

import com.intellij.openapi.extensions.ExtensionPointListener
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginDescriptor
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
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

        fun get(dataType: CwtDataType): List<ParadoxCsvExpressionMatcher> = CACHE.get()?.get(dataType).orEmpty()

        // region Implementations

        init {
            computeCache()
            addListener()
        }

        private fun computeCache() {
            CACHE.reinitialize {
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
                result.mapValues { (_, v) -> v.optimized() }.optimized()
            }
        }

        private fun addListener() {
            EP_NAME.addExtensionPointListener(object : ExtensionPointListener<ParadoxCsvExpressionMatcher> {
                override fun extensionAdded(extension: ParadoxCsvExpressionMatcher, pluginDescriptor: PluginDescriptor) = computeCache()
                override fun extensionRemoved(extension: ParadoxCsvExpressionMatcher, pluginDescriptor: PluginDescriptor) = computeCache()
            })
        }

        // endregion
    }
}
