package icu.windea.pls.config.util.data

import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.delegated.CwtLocationConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.isIdentifier

/**
 * 选项标志（Option Flags）。
 *
 * 成员规则上可以存在多个单独且类似标识符的选项值，用于附加布尔型标志。
 * 本类统一解析这些标志，便于检查特定的状态。
 *
 * @see CwtOptionValueConfig
 */
class CwtOptionFlags private constructor(value: Set<String>) {
    /**
     * 用于在位置规则中，将对应位置的本地化和图片标记为必需项。
     *
     * - 适用对象：位置规则（[CwtLocationConfig]）。
     * - 兼容性：兼容。
     */
    val required = value.contains("required")

    /**
     * 用于在位置规则中，将对应位置的本地化和图片标记为可选项。
     *
     * - 适用对象：位置规则（[CwtLocationConfig]）。
     * - 兼容性：兼容。
     */
    val optional = value.contains("optional")

    /**
     * 用于在位置规则中，将对应位置的本地化和图片标记为主要项。
     * 这意味着它们会作为最相关的本地化和图片，优先显示在快速文档和内嵌提示中。
     *
     * - 适用对象：位置规则（[CwtLocationConfig]）。
     * - 兼容性：兼容。
     */
    val primary = value.contains("primary")

    /**
     * 用于在部分扩展规则中，注明规则上下文与作用域上下文将会被继承。
     * 即，继承自对应的使用处，与其保持一致。
     *
     * - 适用对象：部分可指定规则上下文的扩展规则（如 [CwtExtendedInlineScriptConfig]）。
     * - 兼容性：PLS 扩展。
     *
     * @see CwtExtendedInlineScriptConfig
     * @see CwtExtendedParameterConfig
     */
    val inherit = value.contains("inherit")

    /**
     * 用于将作为单独的值的成员规则标记为预定义的标签。
     * 这会提供特殊的语义高亮与快速文档。
     *
     * - 适用对象：作为单独的值的成员规则（[CwtValueConfig]）。
     * - 兼容性：PLS 扩展。
     *
     * @see CwtTagType
     */
    val tag = value.contains("tag")

    companion object {
        private val EMPTY = CwtOptionFlags(emptySet())

        /**
         * 提取指定的成员规则上的选项标志。
         *
         * 仅收集单独且类似标识符的选项值，如 `## required`。
         *
         * 示例：
         * ```cwt
         * type[technology] = {
         * 	# ...
         * 	localisation = {
         * 		## required
         * 		## primary
         * 		name = "$"
         * 	}
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
