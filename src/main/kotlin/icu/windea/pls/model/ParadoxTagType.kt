package icu.windea.pls.model

/**
 * 标签类型。
 *
 * 用于标记脚本文件中的特殊的值。这会启用特殊的代码高亮和文档注释。
 */
enum class ParadoxTagType(val id: String) {
    /**
     * 预定义的标签。
     * - 适用于：某些拥有特殊用途的单独的字符串，如 `optimize_memory`。
     * - 如何声明：附加 `## tag` 选项到对应的值规则。
     */
    Predefined("tag"),
    /**
     * 类型键前缀。
     * - 适用于：位于特定类型的定义的类型键之前的单独的字符串。
     * - 如何声明：在对应的类型规则中指定 `type_key_prefix = xxx`。
     */
    TypeKeyPrefix("type key prefix"),
    ;

    override fun toString() = id
}
