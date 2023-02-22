package icu.windea.pls.core.search.scopes

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxGameWithDependenciesScope(
    project: Project,
    val gameDirectory: VirtualFile,
    val modDependencyDirectories: Set<VirtualFile>
) : ParadoxGlobalSearchScope(project) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.game.withDependencies", gameDirectory.name)
    }
    
    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(gameDirectory, file, false)
            || VfsUtilCore.isUnder(file, modDependencyDirectories)
    }
    
    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return true
    }
    
    override fun isSearchInLibraries(): Boolean {
        return true
    }
    
    override fun calcHashCode(): Int {
        var result = gameDirectory.hashCode()
        result = result * 31 + modDependencyDirectories.hashCode()
        return result
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxGameWithDependenciesScope && gameDirectory == other.gameDirectory
            && modDependencyDirectories == other.modDependencyDirectories
    }
    
    override fun toString(): String {
        return "Paradox game directory scope: $gameDirectory (with dependencies)"
    }
}
