package icu.windea.pls.core.search.scopes

import com.intellij.model.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxModScope(
    project: Project,
    val modDirectory: VirtualFile
) : ParadoxGlobalSearchScope(project) {
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.mod", modDirectory.name)
    }
    
    override fun getModelBranchesAffectingScope(): Collection<ModelBranch> {
        return ModelBranch.getFileBranch(modDirectory).toSingletonSetOrEmpty()
    }
    
    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(modDirectory, file, false)
    }
    
    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return true
    }
    
    override fun isSearchInLibraries(): Boolean {
        return true
    }
    
    override fun calcHashCode(): Int {
        return modDirectory.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxModScope && modDirectory == other.modDirectory
    }
    
    override fun toString(): String {
        return "Paradox mod directory scope: $modDirectory"
    }
}