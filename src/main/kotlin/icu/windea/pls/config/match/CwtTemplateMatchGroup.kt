package icu.windea.pls.config.match

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression

/**
 * 模板表达式的匹配分组。包含匹配的数据表达式、匹配的文本等信息。
 *
 * @see CwtTemplateExpression
 */
data class CwtTemplateMatchGroup(
    val expression: CwtDataExpression,
    val value: String,
    val offset: Int,
    val incomplete: Boolean = false,
)
