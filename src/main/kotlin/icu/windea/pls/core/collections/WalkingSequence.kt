@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue

/**
 * 带有上下文的序列。
 */
class WalkingSequence<T>(
    private val delegate: Sequence<T> = emptySequence(),
    val context: WalkingContext = WalkingContext()
) : Sequence<T> by delegate

/**
 * [WalkingSequence] 的上下文。
 *
 * 可以携带额外的元数据，从而定制序列的构建逻辑。
 */
class WalkingContext : UserDataHolderBase() {
    object Keys : KeyRegistry()

    @JvmInline
    value class Builder(val context: WalkingContext) {
        inline operator fun <T> plus(value: T): T = value
    }
}

/**
 * 转换 [WalkingSequence] 并保留上下文。
 */
inline fun <T, R> WalkingSequence<T>.transform(block: Sequence<T>.() -> Sequence<R>): WalkingSequence<R> {
    return WalkingSequence(block(this), context)
}

/**
 * 更新 [WalkingSequence] 的上下文。
 */
inline fun <T> WalkingSequence<T>.context(block: WalkingContext.Builder.() -> Unit): WalkingSequence<T> {
    block(WalkingContext.Builder(context))
    return this
}

/** 是否从前往后搜索。默认为 `true`。 */
var WalkingContext.forward: Boolean by registerKey(WalkingContext.Keys) { true }

/** @see WalkingContext.forward */
inline infix fun WalkingContext.Builder.forward(value: Boolean? = true) = apply { value?.let { context.forward = it } }
