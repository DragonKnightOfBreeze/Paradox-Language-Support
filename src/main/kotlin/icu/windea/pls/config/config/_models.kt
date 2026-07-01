package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig

/**
 * @see CwtMemberConfig
 */
enum class CwtMembersType {
    UNSET,
    NONE,
    MIXED,
    PROPERTY,
    VALUE,
}

/**
 * @see CwtMemberConfig
 */
enum class CwtMemberType {
    PROPERTY,
    VALUE,
}

/**
 * 子规则分组。
 *
 * @see CwtSubtypeConfig
 */
enum class CwtSubtypeGroup(val id: String) {
    EventAttribute("event_attribute"),
    EventType("event_type"),
    TechnologyAttribute("technology_attribute"),
    ;

    operator fun contains(config: CwtSubtypeConfig) = config.group == id

    override fun toString() = id
}

/**
 * 行类型。默认为 [Key]。
 *
 * 决定如何匹配其中的每一列。按列名匹配（列名不可重复），还是按列在所在行中的索引匹配（列名可重复）。
 *
 * @see CwtRowConfig
 */
enum class CwtRowType(val id: String) {
    Key("key"), Index("index");

    companion object {
        @JvmStatic
        fun resolve(id: String?): CwtRowType {
            return when (id?.lowercase()) {
                "index" -> Index
                else -> Key
            }
        }
    }
}

/**
 * 链接类型。默认为 [Scope]。
 *
 * @see CwtLinkConfig
 */
enum class CwtLinkType(val id: String) {
    Scope("scope"), Value("value"), Both("both");

    fun forScope() = this == Scope || this == Both
    fun forValue() = this == Value || this == Both

    override fun toString() = id

    companion object {
        @JvmStatic
        fun resolve(id: String?): CwtLinkType {
            return when (id?.lowercase()) {
                "value" -> Value
                "both" -> Both
                else -> Scope
            }
        }
    }
}

/**
 * 动态链接使用的传参分隔符。默认为 [Comma]。
 *
 * @see CwtLinkConfig
 */
enum class CwtLinkArgumentSeparator(val id: String) {
    Comma("comma"), Pipe("pipe");

    fun usePipe() = this == Pipe

    override fun toString() = id

    companion object {
        @JvmStatic
        fun resolve(id: String?): CwtLinkArgumentSeparator {
            return when (id?.lowercase()) {
                "pipe" -> Pipe
                else -> Comma
            }
        }
    }
}
