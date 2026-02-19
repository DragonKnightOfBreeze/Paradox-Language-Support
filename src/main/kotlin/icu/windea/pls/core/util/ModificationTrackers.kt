package icu.windea.pls.core.util

import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import icu.windea.pls.core.EMPTY_OBJECT

/**
 * 合并型修改追踪器。
 *
 * 将多个 [ModificationTracker] 的 `modificationCount` 求和后作为本追踪器的结果。
 */
class MergedModificationTracker(vararg val modificationTrackers: ModificationTracker) : ModificationTracker {
    override fun getModificationCount(): Long {
        return modificationTrackers.sumOf { it.modificationCount }
    }
}

/**
 * 计算型修改追踪器。
 *
 * 每次调用 [getModificationCount] 时执行 [computable] 获取“当前计算值”，若与上次不同则自增一次修改计数。
 */
class ComputedModificationTracker(private val computable: () -> Any?) : SimpleModificationTracker() {
    var computed: Any? = EMPTY_OBJECT

    override fun getModificationCount(): Long {
        val newComputed = computable()
        if (computed != EMPTY_OBJECT && ((computed == null && newComputed == null) || computed != newComputed)) {
            incModificationCount()
        }
        computed = newComputed
        return super.getModificationCount()
    }
}

/**
 * 基于文件路径的修改追踪器。
 */
class FilePathBasedModificationTracker(val patterns: Set<String>) : SimpleModificationTracker() {
    companion object {
        @JvmStatic
        val NEVER_CHANGED = FilePathBasedModificationTracker(emptySet())
    }
}
