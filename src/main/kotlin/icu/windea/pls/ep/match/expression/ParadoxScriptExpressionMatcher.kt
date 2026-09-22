package icu.windea.pls.ep.match.expression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchOptionsService
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 用于匹配脚本表达式与规则表达式。
 *
 * @see ParadoxExpression
 * @see CwtDataExpression
 * @see ParadoxScriptExpressionElement
 * @see ParadoxMatchOptionsService
 * @see ParadoxExpressionMatchService
 */
interface ParadoxScriptExpressionMatcher {
    fun supports(dataType: CwtDataType): Boolean

    /**
     * 匹配脚本表达式和规则表达式。
     */
    fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatcher>("icu.windea.pls.scriptExpressionMatcher")
        @JvmField val CACHE = LazyValue<Map<CwtDataType, List<ParadoxScriptExpressionMatcher>>>()

        @JvmStatic
        fun getAll(dataType: CwtDataType): List<ParadoxScriptExpressionMatcher> = CACHE.get()?.get(dataType).orEmpty()

        // region Implementations

        init {
            CACHE.initialize { computeCache() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { computeCache() } }
        }

        private fun computeCache(): Map<CwtDataType, List<ParadoxScriptExpressionMatcher>> {
            val result = mutableMapOf<CwtDataType, List<ParadoxScriptExpressionMatcher>>()
            val eps = EP_NAME.extensionList
            CwtDataType.entries.values.forEach { dataType -> eps.filterFast { ep -> ep.supports(dataType) }.orNull()?.let { result[dataType] = it.optimized() } }
            return result.optimized()
        }

        // endregion
    }
}
