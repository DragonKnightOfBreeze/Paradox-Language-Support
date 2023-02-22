package icu.windea.pls.core.search.scopes

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxModAndGameScope(
    val modDirectory: VirtualFile,
    val gameDirectory: VirtualFile?,
    project: Project
) : ParadoxGlobalSearchScope(project) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.modAndGame", modDirectory.name, gameDirectory?.name.toString())
    }
    
    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(modDirectory, file, false)
            || (gameDirectory != null  && VfsUtilCore.isAncestor(gameDirectory, file, false))
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
        return result
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxModAndGameScope && modDirectory == other.modDirectory
            && gameDirectory == other.gameDirectory
    }
    
    override fun toString(): String {
        return "Paradox mod and game directory scope: $modDirectory, $gameDirectory"
    }
}