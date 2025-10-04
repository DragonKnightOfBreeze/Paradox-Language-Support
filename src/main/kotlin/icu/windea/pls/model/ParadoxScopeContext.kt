package icu.windea.pls.model

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.scope.ParadoxOverriddenScopeContextProvider
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode

/**
 * 作用域上下文。
 *
 * **作用域上下文（scope context）** 是模组编程中 **谓语**（如触发、效应、修正等）的结构中，某处的当前状态，
 * 保存了 **系统作用域** 到 **作用域** 信息的映射。
 * 以便进行取值、回溯、入栈、替换等操作。
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
 * @see icu.windea.pls.config.config.delegated.CwtScopeConfig
 * @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
 * @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
 * @see icu.windea.pls.lang.util.ParadoxScopeManager
 */
interface ParadoxScopeContext : UserDataHolder {
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

    companion object Resolver

    object Keys : KeyRegistry()
}

/** 需要提升的作用域的 ID 列表。 */
var ParadoxScopeContext.promotions: Set<String> by createKey(ParadoxScopeContext.Keys) { emptySet() }

/** 当前的作用域上下文是否是精确的 - 这意味着不需要再进一步推断其中的各个作用域。 */
var ParadoxScopeContext.isExact: Boolean by createKey(ParadoxScopeContext.Keys) { true }

var ParadoxScopeContext.overriddenProvider: ParadoxOverriddenScopeContextProvider? by createKey(ParadoxScopeContext.Keys)

fun ParadoxScopeContext.toScopeMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, ParadoxScope> {
    return buildMap {
        put("this", scope)
        root?.let { put("root", it.scope) }
        if (showFrom) {
            from?.let { put("from", it.scope) }
            fromFrom?.let { put("fromfrom", it.scope) }
            fromFromFrom?.let { put("fromfromfrom", it.scope) }
            fromFromFromFrom?.let { put("fromfromfromfrom", it.scope) }
        }
        if (showPrev) {
            prev?.let { put("prev", it.scope) }
            prevPrev?.let { put("prevprev", it.scope) }
            prevPrevPrev?.let { put("prevprevprev", it.scope) }
            prevPrevPrevPrev?.let { put("prevprevprevprev", it.scope) }
        }
    }
}

fun ParadoxScopeContext.toScopeIdMap(showFrom: Boolean = true, showPrev: Boolean = true): Map<String, String> {
    return buildMap {
        put("this", scope.id)
        root?.let { put("root", it.scope.id) }
        if (showFrom) {
            from?.let { put("from", it.scope.id) }
            fromFrom?.let { put("fromfrom", it.scope.id) }
            fromFromFrom?.let { put("fromfromfrom", it.scope.id) }
            fromFromFromFrom?.let { put("fromfromfromfrom", it.scope.id) }
        }
        if (showPrev) {
            prev?.let { put("prev", it.scope.id) }
            prevPrev?.let { put("prevprev", it.scope.id) }
            prevPrevPrev?.let { put("prevprevprev", it.scope.id) }
            prevPrevPrevPrev?.let { put("prevprevprevprev", it.scope.id) }
        }
    }
}

// val ParadoxScopeContext.Resolver.Empty: ParadoxScopeContext get() = EmptyParadoxScopeContext

fun ParadoxScopeContext.Resolver.resolve(thisScope: String): ParadoxScopeContext {
    return SimpleParadoxScopeContext(ParadoxScope.of(thisScope))
}

fun ParadoxScopeContext.Resolver.resolve(thisScope: String, rootScope: String? = null): ParadoxScopeContext {
    val scope = ParadoxScope.of(thisScope)
    val root = rootScope?.let { ParadoxScopeContext.resolve(it) }
    return DefaultParadoxScopeContext(scope, root)
}

fun ParadoxScopeContext.Resolver.resolve(map: Map<String, String>): ParadoxScopeContext? {
    val scope = map.get("this")?.let { ParadoxScope.of(it) } ?: return null
    val root = map.get("root")?.let { ParadoxScopeContext.resolve(it) }
    val from = map.get("from")?.let { ParadoxScopeContext.resolve(it) }
    val fromFrom = map.get("fromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val fromFromFrom = map.get("fromfromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val fromFromFromFrom = map.get("fromfromfromfrom")?.let { ParadoxScopeContext.resolve(it) }
    val prev = map.get("prev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrev = map.get("prevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrevPrev = map.get("prevprevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevPrevPrevPrev = map.get("prevprevprevprev")?.let { ParadoxScopeContext.resolve(it) }
    val prevStack = buildList b@{
        // break if previous-prev is null (but next-prev is null or not null)
        prev?.let { add(it) } ?: return@b
        prevPrev?.let { add(it) } ?: return@b
        prevPrevPrev?.let { add(it) } ?: return@b
        prevPrevPrevPrev?.let { add(it) } ?: return@b
    }
    return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
}

fun ParadoxScopeContext.resolveNext(pushScope: String?, isFrom: Boolean = false): ParadoxScopeContext {
    if (pushScope == null) return this // transfer current scope context
    val scope = ParadoxScope.of(pushScope)
    val root = if (isFrom) null else this.root
    val from = if (isFrom) null else this.from
    val fromFrom = if (isFrom) null else this.fromFrom
    val fromFromFrom = if (isFrom) null else this.fromFromFrom
    val fromFromFromFrom = if (isFrom) null else this.fromFromFromFrom
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
}

fun ParadoxScopeContext.resolveNext(scopeContext: ParadoxScopeContext, isFrom: Boolean = false): ParadoxScopeContext {
    val scope = scopeContext.scope
    val root = this.root
    val from = if (isFrom) scopeContext.from else scopeContext.from ?: this.from
    val fromFrom = if (isFrom) scopeContext.fromFrom else scopeContext.fromFrom ?: this.fromFrom
    val fromFromFrom = if (isFrom) scopeContext.fromFromFrom else scopeContext.fromFromFrom ?: this.fromFromFrom
    val fromFromFromFrom = if (isFrom) scopeContext.fromFromFromFrom else scopeContext.fromFromFromFrom ?: this.fromFromFromFrom
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return DefaultParadoxScopeContext(scope, root, from, fromFrom, fromFromFrom, fromFromFromFrom, prevStack)
}

fun ParadoxScopeContext.resolveNext(links: List<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>): ParadoxScopeContext {
    val prevStack = this.prevStack.toMutableList().also { it.add(0, this) }
    return LinkedParadoxScopeContext(links, prevStack)
}

private class SimpleParadoxScopeContext(
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
        return toScopeMap().toString()
    }
}

private class DefaultParadoxScopeContext(
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
        return toScopeMap().toString()
    }
}

private class LinkedParadoxScopeContext(
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
        return toScopeMap().toString()
    }
}

// private val EmptyParadoxScopeContext = SimpleParadoxScopeContext(ParadoxScope.Any)
