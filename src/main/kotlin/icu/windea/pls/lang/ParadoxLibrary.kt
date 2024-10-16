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

    fun computeRoots(): MutableSet<VirtualFile> {
        return runReadAction { doComputeRoots() }
    }

    private fun doComputeRoots(): MutableSet<VirtualFile> {
        //这里仅需要收集不在项目中的游戏目录和模组目录
        val newPaths = mutableSetOf<String>()
        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val allModSettings = getProfilesSettings()
        for (modSettings in allModSettings.modSettings.values) {
            val modDirectory = modSettings.modDirectory ?: continue
            val modFile = modDirectory.toVirtualFile(false) ?: continue
            if (!modFile.exists()) continue
            if (!projectFileIndex.isInContent(modFile)) continue
            run {
                val gameDirectory = modSettings.finalGameDirectory ?: return@run
                if (newPaths.contains(gameDirectory)) return@run
                val gameFile = gameDirectory.toVirtualFile(false) ?: return@run
                if (!gameFile.exists()) return@run
                if (projectFileIndex.isInContent(gameFile)) return@run
                newRoots.add(gameFile)
            }
            for (modDependencySettings in modSettings.modDependencies) {
                val modDependencyDirectory = modDependencySettings.modDirectory ?: continue
                if (newPaths.contains(modDependencyDirectory)) continue
                if (modDependencyDirectory == modDirectory) continue //需要排除这种情况
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: continue
                if (!modDependencyFile.exists()) continue
                if (projectFileIndex.isInContent(modDependencyFile)) continue
                newRoots.add(modDependencyFile)
            }
        }
        for (gameSettings in allModSettings.gameSettings.values) {
            val gameDirectory = gameSettings.gameDirectory ?: continue
            val gameFile = gameDirectory.toVirtualFile(false) ?: continue
            if (!gameFile.exists()) continue
            if (!projectFileIndex.isInContent(gameFile)) continue
            for (modDependencySettings in gameSettings.modDependencies) {
                val modDependencyDirectory = modDependencySettings.modDirectory ?: continue
                if (newPaths.contains(modDependencyDirectory)) continue
                val modDependencyFile = modDependencyDirectory.toVirtualFile(false) ?: continue
                if (!modDependencyFile.exists()) continue
                if (projectFileIndex.isInContent(modDependencyFile)) continue
                newRoots.add(modDependencyFile)
            }
        }
        return newRoots
    }
}
