package icu.windea.pls.core.util

/**
 * 用于避免重复的回调。
 *
 * 通过记录已处理的键，确保同一 [key] 的回调仅执行一次。
 */
class CallbackLock {
    private val keys = mutableSetOf<String>()

    /** 清空已记录的键。*/
    fun reset() {
        keys.clear()
    }

    /**
     * 检查并记录 [key]。
     *
     * 第一次遇到返回 `true`，后续相同键返回 `false`。
     */
    fun check(key: String): Boolean {
        return keys.add(key)
    }
}
