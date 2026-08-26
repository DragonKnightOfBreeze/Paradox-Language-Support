package icu.windea.pls.core.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("unused")
object ProcessorScope {
    // NOTE 3.0.2 这些方法的主要目的就在于 `@BuilderInference`，通过这个注解可以避免不必要的类型声明。

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> findFrom(@BuilderInference buildAction: FindProcessor<T>.() -> Unit): T? {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.find<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> findFrom(@BuilderInference buildAction: FindProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): T? {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.find(predicate).apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> collectFrom(@BuilderInference buildAction: CollectProcessor<T, MutableList<T>>.() -> Unit): MutableList<T> {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.collect<T>().apply(buildAction).collection
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> collectFrom(@BuilderInference buildAction: CollectProcessor<T, MutableList<T>>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): MutableList<T> {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.collect(predicate).apply(buildAction).collection
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> duplicateFrom(@BuilderInference buildAction: DuplicateProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.duplicate<T>().apply(buildAction).duplicated
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> duplicateFrom(@BuilderInference buildAction: DuplicateProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.duplicate(predicate).apply(buildAction).duplicated
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> allFrom(@BuilderInference buildAction: AllProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.all<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> allFrom(@BuilderInference buildAction: AllProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.all(predicate).apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> anyFrom(@BuilderInference buildAction: AnyProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.any<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> anyFrom(@BuilderInference buildAction: AnyProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.any(predicate).apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> noneFrom(@BuilderInference buildAction: NoneProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.none<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> noneFrom(@BuilderInference buildAction: NoneProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.none(predicate).apply(buildAction).result
    }
}
