package icu.windea.pls.core

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 双重检查锁（同步版）。
 *
 * - 使用原子标志位 [flag] 避免重复执行 [action]；
 * - 进入同步块前后各检查一次 [flag]；
 * - 执行异常将被记录为 `WARN` 日志，不会抛出到调用方。
 */
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

/**
 * 双重检查锁（协程版）。
 *
 * - 使用 [Mutex] 与原子标志位 [flag] 共同保证仅执行一次 [action]；
 * - 锁内外各检查一次 [flag]；
 * - 执行异常将被记录为 `WARN` 日志，不会抛出到调用方。
 */
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
