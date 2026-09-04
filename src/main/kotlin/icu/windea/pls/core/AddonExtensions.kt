@file:Suppress("unused")

package icu.windea.pls.core

import icu.windea.pls.ChronicleFacade
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

fun String.indexOfLineEnd(): Int {
    // \n`, `\r`, `\r\n`
    val lineBreakIndex = this.indexOfFirst { it == '\n' || it == '\r' }
    if (lineBreakIndex == -1) return -1
    val lineBreakChar = this[lineBreakIndex]
    if (lineBreakChar == '\n' || lineBreakIndex == this.lastIndex) return lineBreakIndex + 1
    val nextChar = this[lineBreakIndex + 1]
    if (nextChar != '\n') return lineBreakIndex + 1
    return lineBreakIndex + 2
}

fun loadText(path: String, locationClass: Class<*> = ChronicleFacade::class.java): String {
    // 让该死的 Windows 换行符见鬼去吧
    val url = path.toClasspathUrl(locationClass)
    return url.openStream().use { s -> s.bufferedReader().use { r -> r.lineSequence().joinToString("\n") } }
}

inline fun <T> runOnce(marker: AtomicBoolean, action: () -> T): T? {
    if (marker.get()) return null
    val r = action()
    marker.set(true)
    return r
}

inline fun <T> withErrorRef(errorRef: AtomicReference<Throwable>, action: () -> T): Result<T> {
    return runCatchingCancelable { action() }.onFailure { errorRef.compareAndSet(null, it) }
}

@Suppress("NOTHING_TO_INLINE")
inline fun ThreadLocal<Boolean>.hasState(): Boolean {
    return get() == true
}

inline fun <T> ThreadLocal<Boolean>.withState(action: () -> T): T {
    try {
        set(true)
        return action()
    } finally {
        remove()
    }
}
