package icu.windea.pls.lang

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import javax.swing.*

//each library each project

class ParadoxLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    @Volatile var roots: Set<VirtualFile> = emptySet()
    
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
    
    @Suppress("UnstableApiUsage")
    fun refreshRoots() {
        val oldRoots = roots
        val newRoots = computeRoots()
        if(oldRoots == newRoots) return
        roots = newRoots
        runInEdt(ModalityState.nonModal()) {
            runWriteAction {
                val libraryName = PlsBundle.message("library.name")
                AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(project, libraryName, oldRoots, newRoots, libraryName)
            }
        }
    }
    
    fun computeRoots(): Set<VirtualFile> {
        return runReadAction { doComputeRoots() }
    }
    
    private fun doComputeRoots(): Set<VirtualFile> {
        //这里仅需要收集不在项目中的根目录（游戏目录与模组目录）
        
        val newPaths = mutableSetOf<String>()
        val profilesSettings = getProfilesSettings()
        profilesSettings.modSettings.values.forEach f@{ modSettings ->
            val modDirectory = modSettings.modDirectory ?: return@f
            newPaths += modDirectory
            run {
                val gameDirectory = modSettings.finalGameDirectory ?: return@run
                newPaths += gameDirectory
            }
            modSettings.modDependencies.forEach f1@{ modDependencySettings ->
                val modDependencyDirectory = modDependencySettings.modDirectory ?: return@f1
                newPaths += modDependencyDirectory
            }
        }
        profilesSettings.gameSettings.values.forEach f@{ gameSettings ->
            val gameDirectory = gameSettings.gameDirectory ?: return@f
            newPaths += gameDirectory
            gameSettings.modDependencies.forEach f1@{ modDependencySettings ->
                val modDependencyDirectory = modDependencySettings.modDirectory ?: return@f1
                newPaths += modDependencyDirectory
            }
        }
        val newRoots = newPaths.mapNotNull { it.toVirtualFile(false) }
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val result = newRoots
            .filter { !ParadoxCoreManager.isExcludedRootFilePath(it.path) }
            .filter { it.exists() && !projectFileIndex.isInContent(it) }
            .toSet()
        return result
    }
}
