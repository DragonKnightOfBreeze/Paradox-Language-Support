package icu.windea.pls.config.configGroup

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.lang.util.*
import javax.swing.*

//each library each project

class CwtConfigGroupLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    @Volatile
    var roots: Set<VirtualFile> = emptySet()

    override fun getSourceRoots(): Collection<VirtualFile> {
        return roots
    }

    override fun isShowInExternalLibrariesNode(): Boolean {
        return true
    }

    override fun getIcon(unused: Boolean): Icon {
        return PlsIcons.General.Library
    }

    override fun getPresentableText(): String {
        return PlsBundle.message("configGroup.library.name")
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtConfigGroupLibrary && project == other.project)
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }

    override fun toString(): String {
        return "CwtConfigGroupLibrary(project=$project)"
    }

    @Suppress("UnstableApiUsage")
    fun refreshRoots() {
        val oldRoots = roots
        val newRoots = computeRoots()
        if (oldRoots == newRoots) return
        roots = newRoots
        runInEdt(ModalityState.nonModal()) {
            runWriteAction {
                val libraryName = PlsBundle.message("configGroup.library.name")
                AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(project, libraryName, oldRoots, newRoots, libraryName)
            }
        }
    }

    fun computeRoots(): Set<VirtualFile> {
        return runReadAction { doComputeRoots() }
    }

    private fun doComputeRoots(): Set<VirtualFile> {
        //这里仅需要收集不在项目中的根目录（规则目录）
        //即使对应的规则分组未启用，也要显示

        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if(projectFileIndex.isInContent(rootDirectory)) return@f
            newRoots += rootDirectory
        }
        newRoots.removeIf { PlsCoreManager.isExcludedRootFilePath(it.path) }
        return newRoots
    }
}
