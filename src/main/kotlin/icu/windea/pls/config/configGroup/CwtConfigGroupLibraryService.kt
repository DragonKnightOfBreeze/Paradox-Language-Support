package icu.windea.pls.config.configGroup

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsListener
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.lang.util.PlsAnalyzeManager
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class CwtConfigGroupLibraryService(private val project: Project) {
    val library = CwtConfigGroupLibrary(project)

    fun refreshRootsAsync() {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val oldRoots = library.roots
            val newRoots = readAction { computeRoots() }
            if (oldRoots == newRoots) return@launch
            library.roots = newRoots
            edtWriteAction { refreshRoots(oldRoots, newRoots) }
        }
    }

    private fun computeRoots(): Set<VirtualFile> {
        // 这里仅需要收集不在项目中的根目录（规则目录）
        // 即使对应的规则分组未启用，也要显示
        val newRoots = mutableSetOf<VirtualFile>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val fileProviders = CwtConfigGroupFileProvider.INSTANCE.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if (projectFileIndex.isInContent(rootDirectory)) return@f
            newRoots += rootDirectory
        }
        newRoots.removeIf { PlsAnalyzeManager.isExcludedRootFilePath(it.path) }
        return newRoots
    }

    @Suppress("UnstableApiUsage")
    private fun refreshRoots(oldRoots: Set<VirtualFile>, newRoots: Set<VirtualFile>) {
        val libraryName = PlsBundle.message("configGroup.library.name")
        AdditionalLibraryRootsListener.fireAdditionalLibraryChanged(project, libraryName, oldRoots, newRoots, libraryName)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CwtConfigGroupLibraryService = project.service()
    }
}
