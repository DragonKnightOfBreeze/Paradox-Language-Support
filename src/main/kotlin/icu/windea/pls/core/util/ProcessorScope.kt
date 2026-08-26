package icu.windea.pls.core.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("unused")
object ProcessorScope {
    // These methods support several distinct usage styles:
    // - Assertions occur within `predicate`. This style treats `buildAction` as a pure `processProvider`, which is more intuitive.
    // - Assertions occur directly within `buildAction`. This style often requires combining conditional statements, and is more direct.
    // - Assertions occur in both `buildAction` and `predicate`.
    //
    // Examples:
    // - `ProcessorScope.findFrom { processSomething { if (otherPredicate()) process(it) else true } }`
    // - `ProcessorScope.findFrom({ processSomething { process(it) } }) { predicate() }`
    // - `ProcessorScope.findFrom({ processSomething { if (otherPredicate()) process(it) else true } }) { predicate() }`
    //
    // The primary purpose of these methods is the `@BuilderInference` annotation, which helps avoid unnecessary type declarations regardless of the usage style.

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
