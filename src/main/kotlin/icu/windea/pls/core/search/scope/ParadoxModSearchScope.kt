package icu.windea.pls.core.search.scope

import com.intellij.model.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*

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
    
    override fun getModelBranchesAffectingScope(): Collection<ModelBranch> {
        if(modDirectory == null) return emptySet()
        return ModelBranch.getFileBranch(modDirectory).toSingletonSetOrEmpty()
    }
    
    override fun contains(file: VirtualFile): Boolean {
        if(!ParadoxFileHandler.canReference(contextFile, file)) return false //判断上下文文件能否引用另一个文件中的内容
        return modDirectory != null && VfsUtilCore.isAncestor(modDirectory, file, false)
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