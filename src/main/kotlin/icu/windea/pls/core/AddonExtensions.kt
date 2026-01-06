package icu.windea.pls.core

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import java.util.concurrent.atomic.AtomicReference

fun loadText(path: String, locationClass: Class<*> = PlsFacade::class.java): String {
    // 让该死的 Windows 换行符见鬼去吧
    val url = path.toClasspathUrl(locationClass)
    return url.openStream().use { s -> s.bufferedReader().use { r -> r.lineSequence().joinToString("\n") } }
}

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
