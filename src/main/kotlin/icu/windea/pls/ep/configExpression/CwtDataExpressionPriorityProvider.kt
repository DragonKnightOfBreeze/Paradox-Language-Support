package icu.windea.pls.ep.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

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
        val EP_NAME = ExtensionPointName<CwtDataExpressionPriorityProvider>("icu.windea.pls.dataExpressionPriorityProvider")

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
