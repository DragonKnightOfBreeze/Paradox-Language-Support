package icu.windea.pls.core.search.scope

import com.intellij.model.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxModSearchScope(
    project: Project,
    val modDirectory: VirtualFile?
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
        return modDirectory != null && VfsUtilCore.isAncestor(modDirectory, file, false)
    }
    
    override fun calcHashCode(): Int {
        return modDirectory.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxModSearchScope && modDirectory == other.modDirectory
    }
    
    override fun toString(): String {
        return "Paradox mod directory scope: $modDirectory"
    }
}