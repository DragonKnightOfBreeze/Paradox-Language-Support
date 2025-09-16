package icu.windea.pls.config.configExpression

/**
 * CWT 位置表达式。
 *
 * 用于定位目标资源（图片、本地化等）的来源。
 * 如果表达式中包含包含 `$`，视为占位符，需要在后续步骤以“定义名或属性值”等动态内容替换。
 *
 * @property location 原始位置字符串。
 * @property isPlaceholder 是否包含占位符（检测 `$`）。
 *
 * @see icu.windea.pls.config.config.delegated.CwtLocationConfig
 */
interface CwtLocationExpression : CwtConfigExpression {
    val location: String
    val isPlaceholder: Boolean

    operator fun component1() = location
    operator fun component2() = isPlaceholder
}
