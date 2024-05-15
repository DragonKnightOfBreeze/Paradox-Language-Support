package icu.windea.pls.core.util

import com.intellij.openapi.util.*

class MergedModificationTracker(vararg val modificationTrackers: ModificationTracker) : ModificationTracker {
    override fun getModificationCount(): Long {
        return modificationTrackers.sumOf { it.modificationCount }
    }
}

class PathBasedModificationTracker(key: String) : SimpleModificationTracker() {
    val patterns = key.split(';').toSet()
}