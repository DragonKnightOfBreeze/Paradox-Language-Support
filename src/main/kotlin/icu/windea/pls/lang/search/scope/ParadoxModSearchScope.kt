package icu.windea.pls.lang.search.scope

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxModSearchScope(
    project: Project,
    val contextFile: VirtualFile?,
    val modDirectory: VirtualFile?,
) : ParadoxSearchScope(project) {
    @Suppress("DialogTitleCapitalization")
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.mod")
    }
    
    override fun contains(file: VirtualFile): Boolean {
        val contextFile0 = file.findTopHostFileOrThis()
        if(!ParadoxFileHandler.canReference(contextFile, contextFile0)) return false //判断上下文文件能否引用另一个文件中的内容
        return modDirectory != null && VfsUtilCore.isAncestor(modDirectory, contextFile0, false)
    }
    
    override fun calcHashCode(): Int {
        return modDirectory.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxModSearchScope
            && contextFile == other.contextFile
            && modDirectory == other.modDirectory
    }
    
    override fun toString(): String {
        return "Paradox mod directory scope: $modDirectory"
    }
}