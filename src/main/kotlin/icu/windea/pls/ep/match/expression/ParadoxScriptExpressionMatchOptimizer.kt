package icu.windea.pls.ep.match.expression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchOptimizerContext
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 用于优化脚本表达式与规则表达式的匹配逻辑。
 *
 * @see ParadoxExpression
 * @see ParadoxScriptExpressionElement
 * @see ParadoxMatchService
 */
interface ParadoxScriptExpressionMatchOptimizer {
    /**
     * 优化逻辑是否依赖脚本文件中的上下文。
     */
    fun isDynamic(context: ParadoxScriptExpressionMatchOptimizerContext): Boolean = false

    /**
     * 按匹配结果处理了候选的一组成员规则后，进行后续优化。
     *
     * @return 优化后的候选规则列表。如果为 `null`，则表示此扩展点不适用。
     */
    fun <T : CwtMemberConfig<*>> optimize(configs: List<T>, context: ParadoxScriptExpressionMatchOptimizerContext): List<T>?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxScriptExpressionMatchOptimizer>("icu.windea.pls.scriptExpressionMatchOptimizer")
        @JvmField val CACHE = LazyValue<List<ParadoxScriptExpressionMatchOptimizer>>()

        fun getAll(): List<ParadoxScriptExpressionMatchOptimizer> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<ParadoxScriptExpressionMatchOptimizer> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
