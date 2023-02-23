package icu.windea.pls.core.search.scopes

import com.intellij.model.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*

@Suppress("UnstableApiUsage", "EqualsOrHashCode")
class ParadoxGameScope(
    project: Project,
    val gameDirectory: VirtualFile
) : ParadoxGlobalSearchScope(project) {
    @Suppress("DialogTitleCapitalization")
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.game", gameDirectory.name)
    }
    
    override fun getModelBranchesAffectingScope(): Collection<ModelBranch> {
        return ModelBranch.getFileBranch(gameDirectory).toSingletonSetOrEmpty()
    }
    
    override fun contains(file: VirtualFile): Boolean {
        return VfsUtilCore.isAncestor(gameDirectory, file, false)
    }
    
    override fun calcHashCode(): Int {
        return gameDirectory.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxGameScope && gameDirectory == other.gameDirectory
    }
    
    override fun toString(): String {
        return "Paradox game directory scope: $gameDirectory"
    }
}