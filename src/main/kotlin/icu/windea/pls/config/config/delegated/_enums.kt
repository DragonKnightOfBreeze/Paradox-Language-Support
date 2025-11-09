package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.delegated.CwtLinkArgumentSeparator.*
import icu.windea.pls.config.config.delegated.CwtLinkType.*

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

    override fun toString() = id
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
