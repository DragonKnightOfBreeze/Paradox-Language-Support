package icu.windea.pls.lang.search.scope

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle

@Suppress("EqualsOrHashCode")
class ParadoxGameSearchScope(
    project: Project,
    contextFile: VirtualFile?,
    val gameDirectory: VirtualFile?
) : ParadoxSearchScope(project, contextFile) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.game")
    }

    override fun containsFromTop(topFile: VirtualFile): Boolean {
        return gameDirectory != null && VfsUtilCore.isAncestor(gameDirectory, topFile, false)
    }

    override fun calcHashCode(): Int {
        return gameDirectory.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxGameSearchScope
            && contextFile == other.contextFile
            && gameDirectory == other.gameDirectory
    }

    override fun toString(): String {
        return "Paradox game directory scope: $gameDirectory"
    }
}
