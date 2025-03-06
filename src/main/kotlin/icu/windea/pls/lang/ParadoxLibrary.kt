package icu.windea.pls.lang

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.settings.*
import javax.swing.*

//each library each project

class ParadoxLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    @Volatile
    var roots: Set<VirtualFile> = emptySet()

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
        if (oldRoots == newRoots) return
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
        //这里仅需要收集不在项目中的游戏目录和模组目录

        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val profilesSettings = getProfilesSettings()
        profilesSettings.modSettings.values.forEach f@{ modSettings ->
            val modDirectory = modSettings.modDirectory ?: return@f
            val modFile = modDirectory.toVirtualFile(false) ?: return@f
            if(!modFile.exists()) return@f
            if(!projectFileIndex.isInContent(modFile)) return@f
            run {
                val gameDirectory = modSettings.finalGameDirectory ?: return@run
                val gameFile = gameDirectory.toVirtualFile(false) ?: return@run
                if(!gameFile.exists()) return@run
                if(projectFileIndex.isInContent(gameFile)) return@run
                newRoots += gameFile
            }
            modSettings.modDependencies.forEach f1@{ modDependencySettings ->
                val modDependencyDirectory = modDependencySettings.modDirectory ?: return@f1
                if(modDependencyDirectory == modDirectory) return@f1 //需要排除这种情况
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: return@f1
                if(!modDependencyFile.exists()) return@f1
                if(projectFileIndex.isInContent(modDependencyFile)) return@f1
                newRoots += modDependencyFile
            }
        }
        profilesSettings.gameSettings.values.forEach f@{ gameSettings ->
            val gameDirectory = gameSettings.gameDirectory ?: return@f
            val gameFile = gameDirectory.toVirtualFile(false) ?: return@f
            if(!gameFile.exists()) return@f
            if(!projectFileIndex.isInContent(gameFile)) return@f
            gameSettings.modDependencies.forEach f1@{ modDependencySettings ->
                val modDependencyDirectory = modDependencySettings.modDirectory ?: return@f1
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: return@f1
                if(!modDependencyFile.exists()) return@f1
                if(projectFileIndex.isInContent(modDependencyFile)) return@f1
                newRoots += modDependencyFile
            }
        }
        newRoots.removeIf { PlsManager.isExcludedRootFilePath(it.path) }
        return newRoots
    }
}
