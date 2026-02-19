package icu.windea.pls.ep.config.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于合并数据表达式。
 *
 * 根据用法推断上下文规则时，可能需要合并规则、规则表达式以及数据表达式。
 */
interface CwtDataExpressionMerger {
    /**
     * 得到合并后的表达式的字符串。如果不能合并则返回null。
     */
    fun merge(configExpression1: CwtDataExpression, configExpression2: CwtDataExpression, configGroup: CwtConfigGroup): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtDataExpressionMerger>("icu.windea.pls.dataExpressionMerger")
    }
}
