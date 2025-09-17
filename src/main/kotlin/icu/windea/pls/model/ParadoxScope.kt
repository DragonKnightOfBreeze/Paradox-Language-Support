package icu.windea.pls.model

/**
 * 作用域。
 *
 * **作用域（scope）** 是脚本中描述语义的 **主语**，用于确定各种 **谓语**（如触发、效应、修正等）的执行位置。
 *
 * 在表述时，作用域可能指代：
 * - **作用域链接（scope link）** - 用于获取或切换到需要的作用域（如 `root.owner`）。
 * - **作用域类型（scope type）** - 用于分类作用域，通常从上下文推断（如 `country` `planet`）。
 *
 * 以下是一些常见的谓语：
 * - **触发（trigger）** - 用于进行条件判断。
 * - **效应（effect）** - 用于应用游戏逻辑。
 * - **修正（modifier）** - 用于调整游戏数值。
 * - **事件对象（event target）** - 用于保存需要的作用域，以便后续复用。
 * - **变量（variable）** - 用于保存需要的变量（数值、字符串等），以便后续复用。
 *
 * @property id 作用域（类型） ID。
 *
 * @see ParadoxScopeContext
 * @see icu.windea.pls.config.config.delegated.CwtScopeConfig
 * @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
 * @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
 * @see icu.windea.pls.lang.util.ParadoxScopeManager
 */
sealed interface ParadoxScope {
    val id: String

    /**
     * 任意作用域。
     *
     * 表示任何未在规则文件中进一步细分，或未在上下文中进一步推断的作用域。
     */
    object Any : ParadoxScope {
        override val id: String = "any"

        override fun toString() = id
    }

    /**
     * 未知作用域。
     *
     * 表示任何未在规则文件中定义，或无法在上下文中进一步推断的作用域。
     * 如果作用域在经过切换后无法被解析，则会被视为未知作用域（[Unknown]），而非任意作用域（[Any]）。
     * 最常见的情况是通过 `from` 型的系统作用域切换到未被定义或无法推断的作用域。
     */
    object Unknown : ParadoxScope {
        override val id: String = "?"

        override fun toString() = id
    }

    class Default(override val id: String) : ParadoxScope {
        override fun toString() = id
    }

    companion object {
        @JvmStatic
        fun of(id: String): ParadoxScope {
            return when {
                id == Any.id -> Any
                id == Unknown.id -> Unknown
                else -> Default(id)
            }
        }
    }
}
