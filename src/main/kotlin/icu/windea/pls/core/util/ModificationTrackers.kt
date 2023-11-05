package icu.windea.pls.core.util

import com.intellij.openapi.util.*

class PathModificationTracker(keyString: String) : SimpleModificationTracker() {
    val keys = keyString.split('|').map {
        val i = it.indexOf(':')
        if(i == -1) Key(it, emptySet())
        else Key(it.substring(0, i), it.substring(i + 1).lowercase().split(',').toSortedSet())
    }
    
    class Key(val path: String, val extensions: Set<String>)
}

class MergedModificationTracker(vararg val modificationTrackers: ModificationTracker): ModificationTracker {
    override fun getModificationCount(): Long {
        return modificationTrackers.sumOf { it.modificationCount }
    }
}