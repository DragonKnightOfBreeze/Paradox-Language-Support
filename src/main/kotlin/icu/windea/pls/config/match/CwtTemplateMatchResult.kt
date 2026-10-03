package icu.windea.pls.config.match

import icu.windea.pls.config.configExpression.CwtTemplateExpression

/**
 * 模板表达式的匹配结果。包含模板表达式、输入文本、匹配分组等信息。
 *
 * 说明：
 * - 对于完整模式，匹配分组的数量应等于模板表达式中片段的数量。
 * - 对于不完整模式，由于其前缀匹配策略，匹配分组的数量可能小于模板表达式中片段的数量，但至少为 1。
 *
 * @see CwtTemplateExpression
 */
data class CwtTemplateMatchResult(
    val templateExpression: CwtTemplateExpression,
    val value: String,
    val groups: List<CwtTemplateMatchGroup>,
    val incomplete: Boolean = false,
)
