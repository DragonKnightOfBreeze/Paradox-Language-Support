package icu.windea.pls.core.util

/**
 * 用于避免重复的回调。
 */
class CallbackLock {
    private val keys = mutableSetOf<String>()

    fun reset() {
        keys.clear()
    }

    fun check(key: String): Boolean {
        return keys.add(key)
    }
}
