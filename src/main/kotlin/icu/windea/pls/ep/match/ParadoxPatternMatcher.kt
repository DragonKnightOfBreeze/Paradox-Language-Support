package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.match.ParadoxPatternMatchContext
import icu.windea.pls.lang.match.ParadoxPatternMatchService

/**
 * 用于匹配文本与可作为模式来源的规则表达式。
 *
 * 说明：
 * - 适用于部分模式感知的数据类型（如 [CwtDataTypeSets.Pattern]]）。
 * - 不适用于常量数据类型（[CwtDataTypes.Constant]）。
 * - 规则表达式可能表明匹配逻辑是忽略大小写的，然而，也可以显式指定匹配时是否忽略大小写。
 *
 * @see CwtDataExpression
 * @see ParadoxPatternMatchService
 */
interface ParadoxPatternMatcher {
    /**
     * 匹配文本与可作为模式来源的规则表达式。
     */
    fun matches(text: String, ignoreCase: Boolean, context: ParadoxPatternMatchContext): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxPatternMatcher>("icu.windea.pls.patternMatcher")
        @JvmField val CACHE = LazyValue<List<ParadoxPatternMatcher>>()

        fun getAll(): List<ParadoxPatternMatcher> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<ParadoxPatternMatcher> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
