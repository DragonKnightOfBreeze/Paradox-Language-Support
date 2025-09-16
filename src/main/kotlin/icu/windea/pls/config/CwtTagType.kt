package icu.windea.pls.config

/**
 * 标签类型。
 *
 * 用于标记特殊的值规则。
 * 脚本文件中的对应的值会启用特殊的语义高亮与文档注释。
 */
enum class CwtTagType(
    val id: String
) {
    /**
     * 预定义的标签。
     * - 适用于：某些拥有特殊用途的单独的值，如 `optimize_memory`。
     * - 如何声明：附加 `## tag` 选项到对应的值规则。
     */
    Predefined("tag"),
    /**
     * 类型键的前缀。
     * - 适用于：位于特定类型的定义的顶级键之前的单独的值。
     * - 如何声明：在对应的类型规则中指定 `type_key_prefix = xxx`。
     */
    TypeKeyPrefix("type key prefix"),
    ;
}
