package icu.windea.pls.core

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import javax.swing.*

class ParadoxLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    @Volatile var roots: MutableSet<VirtualFile> = mutableSetOf()
    
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
    
    fun computeRoots(): MutableSet<VirtualFile> {
        //这里仅需要收集不在项目中的游戏目录和模组目录
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
                if(projectFileIndex.isInContent(gameFile)) return@run
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
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: continue
                if(!modDependencyFile.isValid) continue
                if(projectFileIndex.isInContent(modDependencyFile)) continue
                newRoots.add(modDependencyFile)
            }
        }
        return newRoots
    }
    
    
    //org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater.doUpdate
    
    @Suppress("UnstableApiUsage")
    fun refreshRoots() {
        val oldRoots = roots
        val newRoots = computeRoots()
        if(oldRoots == newRoots) return
        roots = newRoots
        runInEdt(ModalityState.NON_MODAL) {
            runWriteAction {
                AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(
                    project,
                    PlsBundle.message("library.name"),
                    oldRoots,
                    newRoots,
                    PlsBundle.message("library.name")
                )
            }
        }
    }
}

