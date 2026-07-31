package icu.windea.pls.core.util

import com.intellij.util.Processor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("unused")
object ProcessorScope {
    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> findFrom(@BuilderInference buildAction: Processor<T>.() -> Unit): T? {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.find<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> findFrom(crossinline predicate: (T) -> Boolean = { true }, @BuilderInference buildAction: Processor<T>.() -> Unit): T? {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.find(predicate).apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> collectFrom(@BuilderInference buildAction: Processor<T>.() -> Unit): MutableList<T> {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.collect<T>().apply(buildAction).collection
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> collectFrom(crossinline predicate: (T) -> Boolean = { true }, @BuilderInference buildAction: Processor<T>.() -> Unit): MutableList<T> {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.collect(predicate).apply(buildAction).collection
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> duplicateFrom(crossinline predicate: (T) -> Boolean = { true }, @BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.duplicate(predicate).apply(buildAction).duplicated
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> duplicateFrom(@BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.duplicate<T>().apply(buildAction).duplicated
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> allFrom(@BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.all<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> allFrom(crossinline predicate: (T) -> Boolean = { true }, @BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.all(predicate).apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> anyFrom(@BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.any<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> anyFrom(crossinline predicate: (T) -> Boolean = { true }, @BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.any(predicate).apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> noneFrom(@BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.none<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> noneFrom(crossinline predicate: (T) -> Boolean = { true }, @BuilderInference buildAction: Processor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.none(predicate).apply(buildAction).result
    }
}
