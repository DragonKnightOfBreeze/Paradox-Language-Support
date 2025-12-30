@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

/**
 * 一种特殊的序列，可在 [options] 中携带额外的元数据，并在遍历时使用。这些元数据可用于定制遍历逻辑。
 */
class WalkingSequence<T>(
    val options: WalkingSequenceOptions = WalkingSequenceOptions(),
    private val delegate: Sequence<T> = emptySequence(),
) : Sequence<T> by delegate {
    /**
     * 构建 [WalkingSequence] 的选项。
     */
    inline fun options(block: WalkingSequenceOptionsBuilder.() -> Unit): WalkingSequence<T> {
        WalkingSequenceOptionsBuilder(options).block()
        return this
    }

    /**
     * 对 [WalkingSequence] 进行转换。
     */
    inline fun <R> transform(block: Sequence<T>.() -> Sequence<R>): WalkingSequence<R> {
        return WalkingSequence(options, block(this))
    }
}

/**
 * [WalkingSequence] 的选项。可携带额外的元数据，并在遍历时使用。
 */
class WalkingSequenceOptions : UserDataHolderBase() {
    object Keys : KeyRegistry()
}

/**
 * [WalkingSequenceOptions] 的构建器。
 */
class WalkingSequenceOptionsBuilder(
    val options: WalkingSequenceOptions = WalkingSequenceOptions()
) {
    inline operator fun plus(other: WalkingSequenceOptionsBuilder) = this
}

/** 是否从前往后搜索。 */
var WalkingSequenceOptions.forward: Boolean by createKey(WalkingSequenceOptions.Keys) { true }

/** @see WalkingSequenceOptions.forward */
inline infix fun WalkingSequenceOptionsBuilder.forward(value: Boolean = true) = apply { options.forward = value }
