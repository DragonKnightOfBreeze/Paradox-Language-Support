package icu.windea.pls.config

/**
 * API 状态。
 *
 * 用于标记需要特殊处理的规则。
 */
enum class CwtApiStatus(
    val id: String
) {
    /**
     * 注明该规则已过时，不建议在最新游戏版本中使用。插件仍会解析这些规则。
     * - 适用于：部分可从日志文件生成的规则。
     * - 如何声明：附加 `## api_status = obsolete` 选项到对应的规则。
     */
    Obsolete("obsolete"),
    /**
     * 注明该规则已被移除。插件不会解析这些规则，但是可能仍会从日志文件生成。
     * - 适用于：部分可从日志文件生成的规则。
     * - 如何声明：附加 `## api_status = removed` 选项到对应的规则。
     */
    Removed("removed"),
    /**
     * 注明该规则是有效的，但是不会从日志文件生成。
     * - 适用于：部分可从日志文件生成的规则。
     * - 如何声明：附加 `## api_status = kept` 选项到对应的规则。
     */
    Kept("kept"),
    ;

    companion object {
        private val valueMap = entries.associateBy { it.id }

        @JvmStatic
        fun get(id: String): CwtApiStatus? = valueMap[id]
    }
}
