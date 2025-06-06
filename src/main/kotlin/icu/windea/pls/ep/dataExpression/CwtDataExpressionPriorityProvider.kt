package icu.windea.pls.ep.dataExpression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*

/**
 * 用于得到CWT数据表达式的优先级。
 *
 * 脚本表达式会优先匹配优先级更高的数据表达式。
 */
interface CwtDataExpressionPriorityProvider {
    /**
     * 得到小数表示的优先级。数值越大，优先级越高。数值不为正数时表示此扩展点不适用。
     */
    fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionPriorityProvider>("icu.windea.pls.dataExpressionPriorityProvider")

        /**
         * @see CwtDataExpressionPriorityProvider.getPriority
         */
        fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.getPriority(configExpression, configGroup)
                if (r > 0) return r
            }
            return 0.0
        }
    }
}
