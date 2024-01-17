package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import icu.windea.pls.core.*

/**
 * 当更改默认游戏目录映射后，刷新库信息。
 *
 * @see ParadoxLibrary
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnDefaultGameDirectoriesChangedListener : ParadoxDefaultGameDirectoriesListener {
    override fun onChange(oldGameDirectories: Map<String, String>, gameDirectories: Map<String, String>) {
        val directories = gameDirectories.values.toMutableSet()
        directories.removeAll(oldGameDirectories.values.toSet())
        doUpdateLibrary(directories)
    }
    
    private fun doUpdateLibrary(directories: Set<String>) {
        val roots = directories.mapNotNull { directory -> directory.orNull()?.toVirtualFile(false) }
        if(roots.isEmpty()) return
        for(project in ProjectManager.getInstance().openProjects) {
            if(project.isDisposed) continue
            val isInProject = runReadAction { roots.any { root -> ProjectFileIndex.getInstance(project).isInContent(root) } }
            if(!isInProject) continue
            val paradoxLibrary = project.paradoxLibrary
            paradoxLibrary.refreshRoots()
        }
    }
}
