package icu.windea.pls.config.configExpression

/**
 * CWT位置表达式。用于定位目标资源的来源（如图片、本地化）。
 *
 * - 基础形态由子类型定义：`CwtImageLocationExpression`、`CwtLocalisationLocationExpression`。
 * - 若 `location` 中包含 `$`，视为占位符，需要在后续步骤以“定义名或属性值”等动态内容替换。
 *
 * 参考：`references/cwt/guidance.md`、`docs/zh/config.md`。
 *
 * @property location 原始位置字符串。
 * @property isPlaceholder 是否包含占位符（检测 `$`）。
 */
interface CwtLocationExpression : CwtConfigExpression {
    val location: String
    val isPlaceholder: Boolean

    operator fun component1() = location
    operator fun component2() = isPlaceholder
}
