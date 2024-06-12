package icu.windea.pls.config.configGroup

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.configGroup.*
import javax.swing.*

//each library each project

class CwtConfigGroupLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
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
        return PlsBundle.message("configGroup.library.name")
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtConfigGroupLibrary && project == other.project)
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
                val libraryName = PlsBundle.message("configGroup.library.name")
                AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(project, libraryName, oldRoots, newRoots, libraryName)
            }
        }
    }
    
    fun computeRoots(): MutableSet<VirtualFile> {
        return runReadAction { doComputeRoots() }
    }
    
    private fun doComputeRoots(): MutableSet<VirtualFile> {
        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEachFast f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if(!rootDirectory.exists()) return@f
            if(projectFileIndex.isInContent(rootDirectory)) return@f
            newRoots += rootDirectory
        }
        return newRoots
    }
}