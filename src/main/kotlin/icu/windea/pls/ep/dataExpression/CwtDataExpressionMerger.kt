package icu.windea.pls.ep.dataExpression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*

/**
 * 用于合并CWT数据表达式。
 *
 * 根据使用推断上下文规则时，可能需要合并规则、规则表达式以及数据表达式。
 */
interface CwtDataExpressionMerger {
    /**
     * 得到合并后的表达式的字符串。如果不能合并则返回null。
     */
    fun merge(configExpression1: CwtDataExpression, configExpression2: CwtDataExpression, configGroup: CwtConfigGroup): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionMerger>("icu.windea.pls.dataExpressionMerger")

        /**
         * @see CwtDataExpressionMerger.merge
         */
        fun merge(configExpression1: CwtDataExpression, configExpression2: CwtDataExpression, configGroup: CwtConfigGroup): String? {
            if (configExpression1 == configExpression2) return configExpression1.expressionString
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.merge(configExpression1, configExpression2, configGroup)
                if (r != null) return r
            }
            return null
        }
    }
}
