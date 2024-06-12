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
    val roots: MutableSet<VirtualFile> by lazy { computeRoots() }
    
    override fun getSourceRoots(): MutableCollection<VirtualFile> {
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
    
    fun computeRoots(): MutableSet<VirtualFile> {
        return runReadAction { doComputeRoots() }
    }
    
    @Suppress("UnstableApiUsage")
    fun refreshRoots() {
        val oldRoots = roots
        val newRoots = computeRoots()
        if(oldRoots == newRoots) return
        roots.clear()
        roots += newRoots
        runInEdt(ModalityState.nonModal()) {
            runWriteAction {
                val libraryName = PlsBundle.message("configGroup.library.name")
                AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(project, libraryName, oldRoots, newRoots, libraryName)
            }
        }
    }
    
    private fun doComputeRoots(): MutableSet<VirtualFile> {
        val newRoots = mutableSetOf<VirtualFile>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEachFast f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if(!rootDirectory.exists()) return@f
            newRoots += rootDirectory
        }
        return newRoots
    }
}