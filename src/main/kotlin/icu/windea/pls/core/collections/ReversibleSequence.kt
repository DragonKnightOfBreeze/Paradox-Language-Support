@file:Suppress("unused")

package icu.windea.pls.core.collections

import kotlin.experimental.*

class ReversibleSequence<T>(
    val operator: Boolean = true,
    private val builder: (operator: Boolean) -> Sequence<T>
) : Sequence<T> {
    override fun iterator() = builder(operator).iterator()

    fun reversed() = ReversibleSequence(!operator, builder)
}

@OptIn(ExperimentalTypeInference::class)
fun <T> reversibleSequence(
    operator: Boolean = true,
    @BuilderInference block: suspend SequenceScope<T>.(operator: Boolean) -> Unit
): ReversibleSequence<T> {
    val builder = { operator: Boolean -> sequence { block(operator) } }
    return ReversibleSequence(operator, builder)
}

fun <T> Sequence<T>.reversed(): Sequence<T> = when {
    this is ReversibleSequence -> this.reversed()
    this == emptySequence<T>() -> emptySequence()
    else -> throw UnsupportedOperationException()
}
