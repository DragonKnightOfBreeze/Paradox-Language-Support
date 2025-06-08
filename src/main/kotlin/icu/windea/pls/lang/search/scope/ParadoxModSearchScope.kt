package icu.windea.pls.lang.search.scope

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

@Suppress("EqualsOrHashCode")
class ParadoxModSearchScope(
    project: Project,
    contextFile: VirtualFile?,
    val modDirectory: VirtualFile?,
) : ParadoxSearchScope(project, contextFile) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.mod")
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        return modDirectory != null && VfsUtilCore.isAncestor(modDirectory, topFile, false)
    }

    override fun calcHashCode(): Int {
        return modDirectory.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxModSearchScope
            && contextFile == other.contextFile
            && modDirectory == other.modDirectory
    }

    override fun toString(): String {
        return "Paradox mod directory scope: $modDirectory"
    }
}
