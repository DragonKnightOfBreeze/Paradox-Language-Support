package icu.windea.pls.core.search.scopes

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxModWithDependenciesScope(
    val modDirectory: VirtualFile,
    val gameDirectory: VirtualFile?,
    val modDependencyDirectories: Set<VirtualFile>,
    project: Project
) : ParadoxGlobalSearchScope(project) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.mod.withDependencies", modDirectory.name)
    }
    
    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(modDirectory, file, false)
            || (gameDirectory != null && VfsUtilCore.isAncestor(gameDirectory, file, false))
            || VfsUtilCore.isUnder(file, modDependencyDirectories)
    }
    
    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return true
    }
    
    override fun isSearchInLibraries(): Boolean {
        return true
    }
    
    override fun calcHashCode(): Int {
        var result = modDirectory.hashCode()
        result = result * 31 + gameDirectory.hashCode()
        result = result * 31 + modDependencyDirectories.hashCode()
        return result
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxModWithDependenciesScope && modDirectory == other.modDirectory
            && gameDirectory == other.gameDirectory
            && modDependencyDirectories == other.modDependencyDirectories
    }
    
    override fun toString(): String {
        return "Paradox mod directory scope: $modDirectory (with dependencies)"
    }
}
