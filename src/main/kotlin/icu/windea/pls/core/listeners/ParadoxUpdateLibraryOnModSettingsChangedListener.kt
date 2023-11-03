package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.settings.*

/**
 * 当更改模组配置后，刷新库信息。
 *
 * @see ParadoxLibrary
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnModSettingsChangedListener : ParadoxModSettingsListener {
    override fun onAdd(modSettings: ParadoxModSettingsState) {
        val modDirectory = modSettings.modDirectory ?: return
        val modFile = modDirectory.toVirtualFile(false) ?: return
        doUpdateLibrary(modFile)
    }
    
    //目前不考虑onRemove的情况
    
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val modDirectory = modSettings.modDirectory ?: return
        val modFile = modDirectory.toVirtualFile(false) ?: return
        doUpdateLibrary(modFile)
    }
    
    private fun doUpdateLibrary(root: VirtualFile) {
        for(project in ProjectManager.getInstance().openProjects) {
            if(project.isDisposed) continue
            val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(root) }
            if(!isInProject) continue
            val paradoxLibrary = project.paradoxLibrary
            paradoxLibrary.refreshRoots()
        }
    }
}

