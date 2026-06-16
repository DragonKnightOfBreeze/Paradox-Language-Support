package icu.windea.pls.model.scope

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode

/**
 * 作用域上下文。
 *
 * 作用域上下文（scope context）是各种谓语（如触发器、效果、修正等）的结构中，某处的当前状态。
 * 它会在内部维护系统作用域到作用域信息的映射，以及上一步作用域的上下文栈。
 *
 * 注意：评估得到的作用域上下文，一般是指切换后的上下文。例如，进入脚本成员的子块后的上下文，或者进入链接节点后的上下文。
 *
 * @property scope 当前作用域（即 `this`）。
 * @property root 根作用域（即 `root`）。此作用域等同于当前事件的根作用域，或者由系统预定义（硬编码）。
 * @property from 来源作用域（即 `from`）的上下文。此作用域等同于调用者事件的根作用域，或者由游戏预定义（硬编码）。
 * @property from2 重复2次后的来源作用域（即 `fromfrom`）的上下文。
 * @property from3 重复3次后的来源作用域（即 `fromfromfrom`）的上下文。
 * @property from4 重复4次后的来源作用域（即 `fromfromfromfrom`）的上下文。
 * @property prev 上一步作用域（即 `prev`）的上下文。此作用域等同于切换前的那个作用域。
 * @property prev2 重复2次后的上一步作用域（即 `prevprev`）的上下文。
 * @property prev3 重复3次后的上一步作用域（即 `prevprevprev`）的上下文。
 * @property prev4 重复4次后的上一步作用域（即 `prevprevprevprev`）的上下文。
 * @property prevStack 上一步作用域的上下文栈。
 * @property links 对应的表达式为 [ParadoxScopeFieldExpression] 时，其中的各个 [ParadoxScopeNode] 以及对应的作用域上下文的列表。
 *
 * @see ParadoxScope
 * @see CwtScopeConfig
 * @see CwtScopeGroupConfig
 * @see CwtSystemScopeConfig
 */
sealed interface ParadoxScopeContext : UserDataHolder {
    val scope: ParadoxScope

    val root: ParadoxScopeContext?
    val from: ParadoxScopeContext?
    val from2: ParadoxScopeContext?
    val from3: ParadoxScopeContext?
    val from4: ParadoxScopeContext?
    val prev: ParadoxScopeContext? get() = prevStack.getOrNull(0)
    val prev2: ParadoxScopeContext? get() = prevStack.getOrNull(1)
    val prev3: ParadoxScopeContext? get() = prevStack.getOrNull(2)
    val prev4: ParadoxScopeContext? get() = prevStack.getOrNull(3)

    val prevStack: List<ParadoxScopeContext>
    val links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>

    val rootScope: ParadoxScope? get() = root?.scope
    val fromScope: ParadoxScope? get() = from?.scope
    val from2Scope: ParadoxScope? get() = from2?.scope
    val from3Scope: ParadoxScope? get() = from3?.scope
    val from4Scope: ParadoxScope? get() = from4?.scope
    val prevScope: ParadoxScope? get() = prev?.scope
    val prev2Scope: ParadoxScope? get() = prev2?.scope
    val prev3Scope: ParadoxScope? get() = prev3?.scope
    val prev4Scope: ParadoxScope? get() = prev4?.scope

    fun resolveNext(pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
        return ParadoxScopeResolver.resolveNextScopeContext(this, pushScope, isFrom)
    }

    fun resolveNext(next: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        return ParadoxScopeResolver.resolveNextScopeContext(this, next, isFrom)
    }

    fun resolveNext(links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>): ParadoxScopeContext {
        return ParadoxScopeResolver.resolveNextScopeContext(this, links)
    }

    fun toScopeMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
        return ParadoxScopeResolver.toScopeMap(this, showFrom, showPrev)
    }

    fun toScopeIdMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, String> {
        return ParadoxScopeResolver.toScopeIdMap(this, showFrom, showPrev)
    }

    fun toPresentableString(separator: String = " ", showFrom: Boolean = true, showPrev: Boolean = true): String {
        return ParadoxScopeResolver.toPresentableString(this, separator, showFrom, showPrev)
    }

    class Simple(
        override val scope: ParadoxScope,
        override val root: ParadoxScopeContext? = null,
    ) : UserDataHolderBase(), ParadoxScopeContext {
        override val from: ParadoxScopeContext? get() = null
        override val from2: ParadoxScopeContext? get() = null
        override val from3: ParadoxScopeContext? get() = null
        override val from4: ParadoxScopeContext? get() = null
        override val prevStack: List<ParadoxScopeContext> get() = emptyList()
        override val links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>> get() = emptyList()

        override fun toString() = toPresentableString()
    }

    class Complex(
        override val scope: ParadoxScope,
        override val root: ParadoxScopeContext? = null,
        override val from: ParadoxScopeContext? = null,
        override val from2: ParadoxScopeContext? = null,
        override val from3: ParadoxScopeContext? = null,
        override val from4: ParadoxScopeContext? = null,
        override val prevStack: List<ParadoxScopeContext> = emptyList(),
    ) : UserDataHolderBase(), ParadoxScopeContext {
        override val links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>> get() = emptyList()

        override fun toString() = toPresentableString()
    }

    class Linked(
        override val links: List<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>,
        override val prevStack: List<ParadoxScopeContext> = emptyList(),
    ) : UserDataHolderBase(), ParadoxScopeContext {
        private val last = links.lastOrNull()?.second ?: throw IllegalArgumentException()

        override val scope: ParadoxScope get() = last.scope
        override val root: ParadoxScopeContext? get() = last.root
        override val from: ParadoxScopeContext? get() = last.from
        override val from2: ParadoxScopeContext? get() = last.from2
        override val from3: ParadoxScopeContext? get() = last.from3
        override val from4: ParadoxScopeContext? get() = last.from4

        override fun toString() = toPresentableString()
    }

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun resolveAny(): ParadoxScopeContext {
            return ParadoxScopeResolver.resolveAnyScopeContext()
        }

        @JvmStatic
        fun resolveUnknown(input: ParadoxScopeContext? = null, isFrom: Boolean = false): ParadoxScopeContext {
            return ParadoxScopeResolver.resolveUnknownScopeContext(input, isFrom)
        }

        @JvmStatic
        fun resolve(thisScope: String): ParadoxScopeContext {
            return ParadoxScopeResolver.resolveScopeContext(thisScope)
        }

        @JvmStatic
        fun resolve(thisScope: String, rootScope: String?): ParadoxScopeContext {
            return ParadoxScopeResolver.resolveScopeContext(thisScope, rootScope)
        }

        @JvmStatic
        fun resolve(map: Map<String, String>): ParadoxScopeContext? {
            return ParadoxScopeResolver.resolveScopeContext(map)
        }
    }
}

