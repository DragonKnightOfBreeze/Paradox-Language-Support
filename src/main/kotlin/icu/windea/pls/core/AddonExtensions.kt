package icu.windea.pls.core

import icu.windea.pls.PlsBundle
import java.util.concurrent.atomic.AtomicReference

val String?.errorDetails: String get() = this?.orNull()?.let { PlsBundle.message("error.details", it) }.orEmpty()

inline fun <T> withErrorRef(errorRef: AtomicReference<Throwable>, action: () -> T): Result<T> {
    return runCatchingCancelable { action() }.onFailure { errorRef.compareAndSet(null, it) }
}

inline fun <T> withState(state: ThreadLocal<Boolean>, action: () -> T): T {
    try {
        state.set(true)
        return action()
    } finally {
        state.remove()
    }
}
