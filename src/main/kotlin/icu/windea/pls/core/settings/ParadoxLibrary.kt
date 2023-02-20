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
    @Volatile var roots: MutableSet<VirtualFile> = mutableSetOf()
    
    fun computeRoots(): MutableSet<VirtualFile> {
        val newPaths = mutableSetOf<String>()
        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val allModSettings = getProfilesSettings()
        for(modSettings in allModSettings.modSettings.values) {
            val modDirectory = modSettings.modDirectory ?: continue
            val modFile = modDirectory.toVirtualFile(false) ?: continue
            if(!modFile.isValid) continue
            if(!projectFileIndex.isInContent(modFile)) continue
            run {
                val gameDirectory = modSettings.gameDirectory ?: return@run
                if(newPaths.contains(gameDirectory)) return@run
                val gameFile = gameDirectory.toVirtualFile(false) ?: return@run
                if(!gameFile.isValid) return@run
                if(projectFileIndex.isInContent(modFile)) return@run
                newRoots.add(gameFile)
            }
            for(modDependencySettings in modSettings.modDependencies) {
                val modDependencyDirectory = modDependencySettings.modDirectory ?: continue
                if(newPaths.contains(modDependencyDirectory)) continue
                if(modDependencyDirectory == modDirectory) continue //需要排除这种情况
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: continue
                if(!modDependencyFile.isValid) continue
                if(projectFileIndex.isInContent(modDependencyFile)) continue
                newRoots.add(modDependencyFile)
            }
        }
        for(gameSettings in allModSettings.gameSettings.values) {
            val gameDirectory = gameSettings.gameDirectory ?: continue
            val gameFile = gameDirectory.toVirtualFile(false) ?: continue
            if(!gameFile.isValid) continue
            if(!projectFileIndex.isInContent(gameFile)) continue
            for(modDependencySettings in gameSettings.modDependencies) {
                val modDependencyDirectory = modDependencySettings.modDirectory ?: continue
                if(newPaths.contains(modDependencyDirectory)) continue
                if(modDependencyDirectory == gameDirectory) continue //需要排除这种情况
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: continue
                if(!modDependencyFile.isValid) continue
                if(projectFileIndex.isInContent(modDependencyFile)) continue
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