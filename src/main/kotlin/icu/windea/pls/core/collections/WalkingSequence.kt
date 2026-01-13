@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue

/**
 * 一种特殊的带有上下文的序列。
 */
class WalkingSequence<T>(
    private val delegate: Sequence<T> = emptySequence(),
    val context: WalkingContext = WalkingContext()
) : Sequence<T> by delegate

/**
 * [WalkingSequence] 的上下文。
 *
 * 可携带额外的元数据，并在遍历时使用。这些元数据可用于定制遍历逻辑。
 */
class WalkingContext : UserDataHolderBase() {
    object Keys : KeyRegistry()

    operator fun plus(other: WalkingContext): WalkingContext {
        Keys.copy(other, this)
        return this
    }

    inline operator fun <T> plus(value: T): T = value
}

/**
 * [WalkingContext] 的构建器。
 */
@JvmInline
value class WalkingContextBuilder(val context: WalkingContext) {
    inline operator fun <T> plus(value: T): T = value
}

/**
 * 对 [WalkingSequence] 进行转换。上下文会被保留。
 */
inline fun <T, R> WalkingSequence<T>.transform(block: Sequence<T>.() -> Sequence<R>): WalkingSequence<R> {
    return WalkingSequence(block(this), context)
}

/**
 * 更新 [WalkingSequence] 的上下文。
 */
inline fun <T> WalkingSequence<T>.context(block: WalkingContextBuilder.() -> Unit): WalkingSequence<T> {
    block(WalkingContextBuilder(context))
    return this
}

/** 是否从前往后搜索。默认为 `true`。 */
var WalkingContext.forward: Boolean by registerKey(WalkingContext.Keys) { true }

/** @see WalkingContext.forward */
inline infix fun WalkingContextBuilder.forward(value: Boolean? = true) = apply { value?.let { context.forward = it } }
