package icu.windea.pls.core.settings

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import javax.swing.*

class ParadoxLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    @Volatile var roots: MutableSet<VirtualFile> = computeRoots()
    
    fun computeRoots(): MutableSet<VirtualFile> {
        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val allModSettings = getAllModSettings()
        for(modSettings in allModSettings.settings.values) {
            val modDirectory = modSettings.modDirectory ?: continue
            val modPath = modDirectory.toPathOrNull() ?: continue
            val modFile = VfsUtil.findFile(modPath, true) ?: continue
            if(!modFile.isValid) continue
            if(!projectFileIndex.isInContent(modFile)) continue
            //newRoots.add(modFile) //unnecessary
            run {
                val gameDirectory = modSettings.gameDirectory ?: return@run
                val gamePath = gameDirectory.toPathOrNull() ?: return@run
                val gameFile = VfsUtil.findFile(gamePath, true) ?: return@run
                if(!gameFile.isValid) return@run
                newRoots.add(gameFile)
            }
            for(modDependencySettings in modSettings.modDependencies.values) {
                val modDependencyDirectory = modDependencySettings.modDirectory ?: continue
                val modDependencyPath = modDependencyDirectory.toPathOrNull() ?: continue
                val modDependencyFile = VfsUtil.findFile(modDependencyPath, true) ?: continue
                if(!modDependencyFile.isValid) continue
                newRoots.add(modDependencyFile)
            }
        }
        return newRoots
    }
    
    override fun getSourceRoots(): Collection<VirtualFile> {
        return roots
    }
    
    override fun isShowInExternalLibrariesNode(): Boolean {
        return true
    }
    
    override fun getIcon(unused: Boolean): Icon {
        return PlsIcons.Library
    }
    
    override fun getPresentableText(): String {
        return PlsBundle.message("library.name")
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxLibrary && project == other.project)
    }
    
    override fun hashCode(): Int {
        return project.hashCode()
    }
}