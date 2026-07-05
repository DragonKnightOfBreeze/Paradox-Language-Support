package icu.windea.pls.core.util.recursion

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData

@Optimized
@PublishedApi
internal object RecursionGuardContext {
    private val cache = ThreadLocal.withInitial { FastMap<String, RecursionGuard>() }
    private val cacheKey = createKey<MutableMap<String, RecursionGuard>>("RecursionGuardContext.cacheKey")

    @PublishedApi
    internal fun createRecursionGuard(recursionGuardCache: MutableMap<String, RecursionGuard>, name: String): RecursionGuard {
        val recursionGuard = RecursionGuard(name)
        recursionGuardCache.put(name, recursionGuard)
        return recursionGuard
    }

    @PublishedApi
    internal fun getRecursionGuardCache(): MutableMap<String, RecursionGuard> {
        return cache.get()
    }

    @PublishedApi
    internal fun getContextRecursionGuardCache(context: UserDataHolder): MutableMap<String, RecursionGuard> {
        return context.getOrPutUserData(cacheKey) { FastMap() }
    }

    @PublishedApi
    internal fun clearRecursionGuard(recursionGuardCache: MutableMap<String, RecursionGuard>, name: String) {
        val removed = recursionGuardCache.remove(name)
        removed?.stackTrace?.clear()
        if (recursionGuardCache.isEmpty()) cache.remove()
    }

    @PublishedApi
    internal fun clearContextRecursionGuard(context: UserDataHolder, recursionGuardCache: MutableMap<String, RecursionGuard>, name: String) {
        val removed = recursionGuardCache.remove(name)
        removed?.stackTrace?.clear()
        if (recursionGuardCache.isEmpty()) context.putUserData(cacheKey, null)
    }
}
