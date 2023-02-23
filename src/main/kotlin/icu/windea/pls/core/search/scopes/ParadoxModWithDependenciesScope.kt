package icu.windea.pls.core.search.scopes

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxModWithDependenciesScope(
    project: Project,
    val modDirectory: VirtualFile,
    val gameDirectory: VirtualFile?,
    val modDependencyDirectories: Set<VirtualFile>
) : ParadoxGlobalSearchScope(project) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.mod.withDependencies", modDirectory.name)
    }
    
    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(modDirectory, file, false)
            || (gameDirectory != null && VfsUtilCore.isAncestor(gameDirectory, file, false))
            || VfsUtilCore.isUnder(file, modDependencyDirectories)
    }
    
    override fun compare(file1: VirtualFile, file2: VirtualFile): Int {
        val order1 = getOrder(file1)
        val order2 = getOrder(file2)
        return order1.compareTo(order2)
    }
    
    private fun getOrder(file: VirtualFile) : Int {
        //-1 - 找不到rootFile
        //0 - 位于游戏目录下
        val rootFile = file.fileInfo?.rootInfo?.rootFile ?: return -1
        if(rootFile == gameDirectory) return 0
        val i = modDependencyDirectories.indexOf(rootFile)
        return if(i != -1) i + 1 else -1
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
