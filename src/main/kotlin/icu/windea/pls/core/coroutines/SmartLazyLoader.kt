package icu.windea.pls.core.coroutines

import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

class SmartLazyLoader<K : Any, V>(
    private val coroutineScopeProvider: () -> CoroutineScope,
    private val awaitTimeoutMs: Long = 200L,
    private val jobTimeoutMs: Long = 30_000L,
    private val circuitBreakerWindowMs: Long = 5_000L,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val onFailure: (key: K, error: Throwable) -> Unit = { _, _ -> }
) {
    data class CachedValue<V>(val isCached: Boolean, val value: V?)

    private val jobs = ConcurrentHashMap<K, Deferred<V?>>()
    private val circuitBreakerUntil = ConcurrentHashMap<K, Long>()

    fun getOrNull(key: K, tryLoad: Boolean = true, cache: () -> CachedValue<V>, loader: () -> V?): V? {
        val cached = cache()
        if (cached.isCached) return cached.value

        if (!tryLoad) {
            schedule(key, cache) { loader() }
            return null
        }

        val deferred = schedule(key, cache) { loader() }
        if (deferred == null) {
            val cachedNow = cache()
            return if (cachedNow.isCached) cachedNow.value else null
        }

        return runBlocking { withTimeoutOrNull(awaitTimeoutMs) { deferred.await() } }
    }

    fun schedule(key: K, cache: () -> CachedValue<V>, loader: () -> V?): Deferred<V?>? {
        val now = System.currentTimeMillis()
        val breakerUntil = circuitBreakerUntil[key]
        if (breakerUntil != null && breakerUntil > now) return null

        val cached = cache()
        if (cached.isCached) return null

        return jobs.computeIfAbsent(key) {
            val coroutineScope = coroutineScopeProvider()
            coroutineScope.async(dispatcher, start = CoroutineStart.DEFAULT) {
                try {
                    withTimeout(jobTimeoutMs) {
                        loader()
                    }
                } catch (e: ProcessCanceledException) {
                    throw e
                } catch (e: TimeoutCancellationException) {
                    onFailure(key, e)
                    circuitBreakerUntil[key] = System.currentTimeMillis() + circuitBreakerWindowMs
                    null
                } catch (e: CancellationException) {
                    // cancelled due to scope disposal etc.
                    throw e
                } catch (e: Exception) {
                    onFailure(key, e)
                    circuitBreakerUntil[key] = System.currentTimeMillis() + circuitBreakerWindowMs
                    null
                } finally {
                    jobs.remove(key)
                }
            }
        }
    }

    fun clear(key: K) {
        jobs.remove(key)
        circuitBreakerUntil.remove(key)
    }

    fun clearAll() {
        jobs.clear()
        circuitBreakerUntil.clear()
    }
}
