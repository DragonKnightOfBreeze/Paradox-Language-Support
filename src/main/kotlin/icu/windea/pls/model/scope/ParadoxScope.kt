package icu.windea.pls.model.scope

import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 作用域。
 *
 * 作用域（scope）是描述语义的主语，用于确定各种谓语（如触发器、效果、修正等）的执行位置。
 *
 * 在表述时，作用域可能指代：
 * - **作用域链接（scope link）**：用于获取或切换到需要的作用域（如 `root` `owner`）。
 * - **作用域类型（scope type）**：用于分类作用域，通常从上下文推断（如 `country` `planet`）。
 *
 * 以下是一些常见的谓语：
 * - **触发器（trigger）**：用于进行条件判断。
 * - **效果（effect）**：用于应用游戏逻辑。
 * - **修正（modifier）**：用于调整游戏数值。
 * - **事件对象（event target）**：用于保存需要的作用域，以便后续复用。
 * - **变量（variable）**：用于保存需要的变量（数值、字符串等），以便后续复用。
 *
 * @property id 作用域的ID。指示作用域类型。
 *
 * @see ParadoxScopeContext
 * @see CwtScopeConfig
 * @see CwtScopeGroupConfig
 * @see CwtSystemScopeConfig
 */
sealed interface ParadoxScope {
    val id: String

    fun isUnsure(): Boolean

    /**
     * 默认作用域。
     */
    class Default(override val id: String) : ParadoxScope {
        override fun isUnsure(): Boolean = false

        override fun toString() = id
    }

    /**
     * 任意作用域。
     *
     * 表示任何未在规则文件中进一步细分，或未在上下文中进一步推断的作用域。
     */
    object Any : ParadoxScope {
        override val id: String = ParadoxScopeConstants.anyScope

        override fun isUnsure(): Boolean = true

        override fun toString() = id
    }

    /**
     * 未知作用域。
     *
     * 表示任何未在规则文件中定义，或无法在上下文中进一步推断的作用域。
     * 如果作用域在经过切换后无法被解析，则会被视为未知作用域（[Unknown]），而非任意作用域（[Any]）。
     * 最常见的情况是通过 `from` 型系统作用域切换到未被定义或无法推断的作用域。
     */
    object Unknown : ParadoxScope {
        override val id: String = ParadoxScopeConstants.unknownScope

        override fun isUnsure(): Boolean = true

        override fun toString() = id
    }

    companion object {
        /**
         * 得到规范化后的作用域的 ID（snake_case）。
         */
        @JvmStatic
        fun getId(scope: String): String {
            return ParadoxScopeResolver.getScopeId(scope)
        }

        /**
         * 得到用于展示的作用域的名字（Capitalized Words）。
         */
        @JvmStatic
        fun getName(scope: String, configGroup: CwtConfigGroup): String {
            return ParadoxScopeResolver.getScopeName(scope, configGroup)
        }

        @JvmStatic
        fun resolve(id: String): ParadoxScope {
            return ParadoxScopeResolver.resolveScope(id)
        }
    }
}
