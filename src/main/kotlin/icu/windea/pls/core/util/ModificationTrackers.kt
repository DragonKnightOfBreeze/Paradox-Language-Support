package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import icu.windea.pls.core.*

class MergedModificationTracker(vararg val modificationTrackers: ModificationTracker) : ModificationTracker {
    override fun getModificationCount(): Long {
        return modificationTrackers.sumOf { it.modificationCount }
    }
}

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

class FilePathBasedModificationTracker(key: String) : SimpleModificationTracker() {
    val patterns = key.split(';').toSet()
}
