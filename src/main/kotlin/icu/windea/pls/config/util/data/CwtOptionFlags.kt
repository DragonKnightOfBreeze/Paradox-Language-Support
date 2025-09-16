package icu.windea.pls.config.util.data

import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigCollector
import icu.windea.pls.config.config.delegated.impl.CwtExtendedParameterConfigResolverImpl
import icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.isIdentifier

/**
 * 选项标志（Option Flags）。
 *
 * 概述：
 * - 在 `.cwt` 规则中，某些“无键的选项值”（如 `## required`、`## primary`）用于给规则项增加布尔型标志。
 * - 本类将这些标志统一解析为布尔字段，便于在各解析/提示流程中快速判断。
 * - 支持的标志包括：`required`、`optional`、`primary`、`inherit`、`tag`。
 *
 * 兼容性：
 * - 与 CWTools 的同名标志兼容；个别标志为 PLS 扩展，具体见对应属性的文档注释。
 */
class CwtOptionFlags private constructor(value: Set<String>) {
    /**
     * 是否标记为必需。
     *
     * - 兼容性：兼容。常见于本地化位置条目（Localisation）。
     *
     * @see CwtLocationConfigResolverImpl
     */
    val required = value.contains("required")

    /**
     * 是否标记为可选。
     *
     * - 兼容性：PLS 扩展。在部分场景可与 `cardinality` 互补/冗余。
     *
     * @see CwtLocationConfigResolverImpl
     */
    val optional = value.contains("optional")

    /**
     * 是否标记为主要（Primary）。
     *
     * - 兼容性：兼容。常用于本地化的主要展示文本标记。
     *
     * @see CwtLocationConfigResolverImpl
     */
    val primary = value.contains("primary")

    /**
     * 是否启用继承（inherit）。
     *
     * - 兼容性：PLS 扩展。用于参数等场景以继承其上下文配置与作用域上下文。
     *
     * @see CwtExtendedParameterConfigResolverImpl
     */
    val inherit = value.contains("inherit")

    /**
     * 是否为标签（tag）。
     *
     * - 兼容性：PLS 扩展。用于把值规则标记为“预定义标签”，影响类型标记与 UI 展示（[CwtTagType]）。
     *
     * @see CwtConfigCollector
     * @see CwtTagType
     */
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
