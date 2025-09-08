package icu.windea.pls.core

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
inline fun <T> Any.withDoubleLock(flag: AtomicBoolean, action: () -> T) {
    if (flag.get()) return
    synchronized(this) {
        if (flag.get()) return
        try {
            action()
            flag.set(true)
        } catch (e: Exception) {
            flag.thisLogger().warn(e)
        }
    }
}

suspend inline fun <T> Mutex.withDoubleLock(flag: AtomicBoolean, action: () -> T) {
    if (flag.get()) return
    withLock {
        if (flag.get()) return
        try {
            action()
            flag.set(true)
        } catch (e: Exception) {
            flag.thisLogger().warn(e)
        }
    }
}
