package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.isIdentifier

/**
 * 选项标志（Option Flags）。
 *
 * 描述与用处：
 * - 在 `.cwt` 规则中，某些“无键的选项值”（形如 `## required`、`## primary`）用于给规则项增加布尔型标志。
 * - 本类将这些标志统一解析为布尔字段，便于在各解析/提示流程中快速判断。
 * - PLS 对以下标志提供支持：`required`、`optional`、`primary`、`inherit`、`tag`。
 *
 * 差异与兼容性：
 * - `required`、`primary`：与 CWTools 行为一致，常用于 Localisation 规则；PLS 亦用于其他扩展场景（如位置字段）。
 * - `optional`：PLS 扩展；有些场景可用 `cardinality` 表达“可选”，但 PLS 仍允许显式 `## optional` 参与本地判定。
 * - `inherit`：PLS 扩展；用于参数（parameters）等场景，指示继承上下文配置（见 `CwtExtendedParameterConfigResolverImpl`）。
 * - `tag`：PLS 扩展；用于将 `CwtValueConfig` 标记为“预定义标签”（见 `CwtConfigCollector.applyTagOption`）。
 *
 * 示例（.cwt）：
 * ```cwt
 * localisation = {
 *     name = "$"
 *     ## required
 *     ## primary
 *     desc = "$_desc"
 * }
 * ```
 */
class CwtOptionFlags private constructor(value: Set<String>) {
    /** 是否标记为必需。与 CWTools 规则兼容，常见于 Localisation 条目。 */
    val required = value.contains("required")

    /** 是否标记为可选。PLS 扩展；在部分地方与 `cardinality` 可互补/冗余。 */
    val optional = value.contains("optional")

    /** 是否标记为主要（Primary）。与 CWTools 规则兼容，常见于 Localisation 的主要展示文本。 */
    val primary = value.contains("primary")

    /** 是否启用继承（inherit）。PLS 扩展；用于参数等场景以继承其上下文配置与作用域上下文。 */
    val inherit = value.contains("inherit")

    /** 是否为标签（tag）。PLS 扩展；用于把值规则标记为“预定义标签”，影响类型标记与 UI 展示。 */
    val tag = value.contains("tag")

    companion object {
        private val EMPTY = CwtOptionFlags(emptySet())

        /**
         * 从给定的成员规则中提取选项标志。
         *
         * 实现说明：
         * - 仅收集“无键的选项值”（`CwtOptionValueConfig`），并要求其文本是合法标识符（`isIdentifier()`）。
         * - 支持的典型写法：`## required`、`## primary`、`## tag`、`## inherit` 等。
         *
         * 示例：
         * ```cwt
         * some_rule = {
         *     ## required
         *     ## primary
         *     value = scalar
         * }
         * ```
         */
        fun from(config: CwtMemberConfig<*>): CwtOptionFlags {
            val optionConfigs = config.optionConfigs ?: return EMPTY
            val flags = optionConfigs.filterIsInstance<CwtOptionValueConfig>()
                .mapNotNullTo(mutableSetOf()) { it.stringValue?.orNull()?.takeIf { s -> s.isIdentifier() } }
            if (flags.isEmpty()) return EMPTY
            return CwtOptionFlags(flags)
        }
    }
}
