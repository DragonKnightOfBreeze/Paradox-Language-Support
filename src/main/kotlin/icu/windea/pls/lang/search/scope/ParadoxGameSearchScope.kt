package icu.windea.pls.lang.search.scope

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*

class ParadoxGameSearchScope(
    project: Project,
    val contextFile: VirtualFile?,
    val gameDirectory: VirtualFile?
) : ParadoxSearchScope(project) {
    @Suppress("DialogTitleCapitalization")
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.game")
    }
    
    override fun contains(file: VirtualFile): Boolean {
        val contextFile0 = file.findTopHostFileOrThis()
        if(!ParadoxFileManager.canReference(contextFile, contextFile0)) return false //判断上下文文件能否引用另一个文件中的内容
        return gameDirectory != null && VfsUtilCore.isAncestor(gameDirectory, contextFile0, false)
    }
    
    override fun calcHashCode(): Int {
        return gameDirectory.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxGameSearchScope
            && contextFile == other.contextFile
            && gameDirectory == other.gameDirectory
    }
    
    override fun toString(): String {
        return "Paradox game directory scope: $gameDirectory"
    }
}
