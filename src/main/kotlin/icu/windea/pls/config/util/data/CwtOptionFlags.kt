package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.isIdentifier

/**
 * 选项标志。即一组类似标识符的字符串，可用来检查特定的状态。
 */
class CwtOptionFlags private constructor(value: Set<String>) {
    val required = value.contains("required")
    val optional = value.contains("optional")
    val primary = value.contains("primary")
    val inherit = value.contains("inherit")
    val tag = value.contains("tag")

    companion object {
        private val EMPTY = CwtOptionFlags(emptySet())

        fun from(config: CwtMemberConfig<*>): CwtOptionFlags {
            val optionConfigs = config.optionConfigs ?: return EMPTY
            val flags = optionConfigs.filterIsInstance<CwtOptionValueConfig>()
                .mapNotNullTo(mutableSetOf()) { it.stringValue?.orNull()?.takeIf { s -> s.isIdentifier() } }
            if (flags.isEmpty()) return EMPTY
            return CwtOptionFlags(flags)
        }
    }
}
