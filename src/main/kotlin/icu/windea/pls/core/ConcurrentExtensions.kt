package icu.windea.pls.core

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
inline fun <T> AtomicBoolean.withLock(lock: Any, action: () -> T) {
    if (get()) return
    synchronized(lock) {
        if (get()) return
        try {
            action()
            set(true)
        } catch (e: Exception) {
            thisLogger().warn(e)
        }
    }
}

suspend inline fun <T> AtomicBoolean.withLock(mutex: Mutex, action: () -> T) {
    if (get()) return
    mutex.withLock {
        if (get()) return
        try {
            action()
            set(true)
        } catch (e: Exception) {
            thisLogger().warn(e)
        }
    }
}
