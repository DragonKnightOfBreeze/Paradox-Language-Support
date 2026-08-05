package icu.windea.pls.core.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("unused")
object ProcessorScope {
    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> findFrom(@BuilderInference source: FindProcessor<T>.() -> Unit): T? {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.find<T>().apply(source).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> findFrom(@BuilderInference source: FindProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): T? {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.find(predicate).apply(source).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> collectFrom(@BuilderInference buildAction: CollectProcessor<T, MutableList<T>>.() -> Unit): MutableList<T> {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.collect<T>().apply(buildAction).collection
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> collectFrom(@BuilderInference source: CollectProcessor<T, MutableList<T>>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): MutableList<T> {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.collect(predicate).apply(source).collection
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> duplicateFrom(@BuilderInference source: DuplicateProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.duplicate<T>().apply(source).duplicated
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> duplicateFrom(@BuilderInference source: DuplicateProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.duplicate(predicate).apply(source).duplicated
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> allFrom(@BuilderInference buildAction: AllProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(buildAction, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.all<T>().apply(buildAction).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> allFrom(@BuilderInference source: AllProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.all(predicate).apply(source).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> anyFrom(@BuilderInference source: AnyProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.any<T>().apply(source).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> anyFrom(@BuilderInference source: AnyProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.any(predicate).apply(source).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> noneFrom(@BuilderInference source: NoneProcessor<T>.() -> Unit): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.none<T>().apply(source).result
    }

    @OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
    inline fun <T> noneFrom(@BuilderInference source: NoneProcessor<T>.() -> Unit, crossinline predicate: (T) -> Boolean = { true }): Boolean {
        contract {
            callsInPlace(source, InvocationKind.EXACTLY_ONCE)
        }
        return ProcessorFactory.none(predicate).apply(source).result
    }
}
