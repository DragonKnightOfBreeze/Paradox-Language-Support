package icu.windea.pls.ep.match.expression

import com.intellij.openapi.extensions.ExtensionPointListener
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginDescriptor
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.match.ParadoxPatternMatchService
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 用于匹配脚本表达式与规则表达式。
 *
 * @see ParadoxExpression
 * @see CwtDataExpression
 * @see ParadoxScriptExpressionElement
 * @see ParadoxMatchService
 * @see ParadoxExpressionMatchService
 */
interface ParadoxScriptExpressionMatcher {
    /**
     * 是否支持将规则表达式作为通配符，然后再进行匹配。
     *
     * @see ParadoxPatternMatchService
     */
    fun isPatternAware(context: ParadoxScriptExpressionMatchContext): Boolean = false

    /**
     * 匹配脚本表达式和规则表达式。
     */
    fun match(context: ParadoxScriptExpressionMatchContext): ParadoxMatchResult?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatcher>("icu.windea.pls.scriptExpressionMatcher")
        @JvmField val CACHE = LazyValue<Map<CwtDataType, List<ParadoxScriptExpressionMatcher>>>()

        fun get(dataType: CwtDataType): List<ParadoxScriptExpressionMatcher> = CACHE.get()?.get(dataType).orEmpty()

        // region Implementations

        init {
            computeCache()
            addListener()
        }

        private fun computeCache() {
            CACHE.reinitialize {
                val result = mutableMapOf<CwtDataType, MutableList<ParadoxScriptExpressionMatcher>>()
                val eps = EP_NAME.extensionList
                eps.forEachFast { ep ->
                    when (ep) {
                        is ParadoxScriptCompositeExpressionMatcher -> {
                            val matchers = ep.matcherMap
                            matchers.forEach { (matcher, dataTypes) ->
                                dataTypes.forEach { dataType ->
                                    result.computeIfAbsent(dataType) { mutableListOf() } += matcher
                                }
                            }
                        }
                        is ParadoxScriptSimpleExpressionMatcher -> {
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
            EP_NAME.addExtensionPointListener(object : ExtensionPointListener<ParadoxScriptExpressionMatcher> {
                override fun extensionAdded(extension: ParadoxScriptExpressionMatcher, pluginDescriptor: PluginDescriptor) = computeCache()
                override fun extensionRemoved(extension: ParadoxScriptExpressionMatcher, pluginDescriptor: PluginDescriptor) = computeCache()
            })
        }

        // endregion
    }
}
