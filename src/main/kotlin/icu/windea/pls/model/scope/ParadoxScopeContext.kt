package icu.windea.pls.model.scope

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

/**
 * 作用域上下文。
 *
 * **作用域上下文（scope context）** 是模组编程中 **谓语**（如触发器、效果、修正等）的结构中，某处的当前状态，
 * 保存了 **系统作用域** 到 **作用域** 信息的映射。
 * 以便进行取值、回溯、入栈、替换等操作。
 *
 * 注意：求值得到的作用域上下文，一般是指切换后的上下文。即，进入脚本成员的子块后，或者进入链接节点后得到的上下文。
 *
 * @property scope 当前作用域（即 `this`）。
 * @property root 根作用域（即 `root`）。
 * @property from 来源作用域（即 `from`）。等同于调用者事件的根作用域，或者由游戏预定义（硬编码）。
 * @property fromFrom 堆叠1次后的来源作用域（即 `fromFrom`）。
 * @property fromFromFrom 堆叠2次后的来源作用域（即 `fromFromFrom`）。
 * @property fromFromFromFrom 堆叠3次后的来源作用域（即 `fromFromFromFrom`）。
 * @property prevStack 上一步作用域的上下文的栈。
 * @property prev 上一步作用域（即 `prev`）的上下文。等同于切换前的那个作用域。
 * @property prevPrev 堆叠1次后的上一步作用域（即 `prevPrev`）的上下文。
 * @property prevPrevPrev 堆叠2次后的上一步作用域（即 `prevPrevPrev`）的上下文。
 * @property prevPrevPrevPrev 堆叠3次后的上一步作用域（即 `prevPrevPrevPrev`）的上下文。
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
    val fromFrom: ParadoxScopeContext?
    val fromFromFrom: ParadoxScopeContext?
    val fromFromFromFrom: ParadoxScopeContext?

    val prevStack: List<ParadoxScopeContext>
    val prev: ParadoxScopeContext? get() = prevStack.getOrNull(0)
    val prevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(1)
    val prevPrevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(2)
    val prevPrevPrevPrev: ParadoxScopeContext? get() = prevStack.getOrNull(3)

    /** 对应的表达式为 [ParadoxScopeFieldExpression] 时，其中的各个 [ParadoxScopeLinkNode] 以及对应的作用域上下文的列表。 */
    val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>

    fun resolveNext(pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
        return ParadoxScopeContextResolver.resolveNext(this, pushScope, isFrom)
    }

    fun resolveNext(next: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
        return ParadoxScopeContextResolver.resolveNext(this, next, isFrom)
    }

    fun resolveNext(links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>): ParadoxScopeContext {
        return ParadoxScopeContextResolver.resolveNext(this, links)
    }

    class Simple(
        override val scope: ParadoxScope
    ) : UserDataHolderBase(), ParadoxScopeContext {
        override val root: ParadoxScopeContext? get() = null
        override val from: ParadoxScopeContext? get() = null
        override val fromFrom: ParadoxScopeContext? get() = null
        override val fromFromFrom: ParadoxScopeContext? get() = null
        override val fromFromFromFrom: ParadoxScopeContext? get() = null
        override val prevStack: List<ParadoxScopeContext> get() = emptyList()
        override val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>> get() = emptyList()

        override fun toString(): String {
            return "SimpleParadoxScopeContext: " + toScopeIdMap().toString()
        }
    }

    class Default(
        override val scope: ParadoxScope,
        override val root: ParadoxScopeContext? = null,
        override val from: ParadoxScopeContext? = null,
        override val fromFrom: ParadoxScopeContext? = null,
        override val fromFromFrom: ParadoxScopeContext? = null,
        override val fromFromFromFrom: ParadoxScopeContext? = null,
        override val prevStack: List<ParadoxScopeContext> = emptyList()
    ) : UserDataHolderBase(), ParadoxScopeContext {
        override val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>> get() = emptyList()

        override fun toString(): String {
            return "DefaultParadoxScopeContext: " + toScopeIdMap().toString()
        }
    }

    class Linked(
        override val links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>,
        override val prevStack: List<ParadoxScopeContext> = emptyList()
    ) : UserDataHolderBase(), ParadoxScopeContext {
        private val last = links.lastOrNull()?.second ?: throw IllegalArgumentException()

        override val scope: ParadoxScope get() = last.scope
        override val root: ParadoxScopeContext? get() = last.root
        override val from: ParadoxScopeContext? get() = last.from
        override val fromFrom: ParadoxScopeContext? get() = last.fromFrom
        override val fromFromFrom: ParadoxScopeContext? get() = last.fromFromFrom
        override val fromFromFromFrom: ParadoxScopeContext? get() = last.fromFromFromFrom

        override fun toString(): String {
            return "LinkedParadoxScopeContext: " + toScopeIdMap().toString()
        }
    }

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun getAny(): ParadoxScopeContext {
            return ParadoxScopeContextResolver.getAny()
        }

        @JvmStatic
        fun getUnknown(input: ParadoxScopeContext? = null, isFrom: Boolean = false): ParadoxScopeContext {
            return ParadoxScopeContextResolver.getUnknown(input, isFrom)
        }

        @JvmStatic
        fun get(thisScope: String): ParadoxScopeContext {
            return ParadoxScopeContextResolver.get(thisScope)
        }

        @JvmStatic
        fun get(thisScope: String, rootScope: String?): ParadoxScopeContext {
            return ParadoxScopeContextResolver.get(thisScope, rootScope)
        }

        @JvmStatic
        fun get(map: Map<String, String>): ParadoxScopeContext? {
            return ParadoxScopeContextResolver.get(map)
        }
    }
}

