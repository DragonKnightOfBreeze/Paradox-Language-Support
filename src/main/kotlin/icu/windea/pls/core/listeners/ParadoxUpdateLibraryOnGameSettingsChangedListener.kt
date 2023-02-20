package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*

/**
 * 当更改游戏配置后，更新库信息。
 *
 * @see ParadoxLibrary
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnGameSettingsChangedListener : ParadoxGameSettingsListener {
    override fun onAdd(gameSettings: ParadoxGameSettingsState) {
        val gameDirectory = gameSettings.gameDirectory ?: return
        val gameFile = gameDirectory.toVirtualFile(false) ?: return
        doUpdate(gameFile)
    }
    
    //目前不考虑onRemove的情况
    
    override fun onChange(gameSettings: ParadoxGameSettingsState) {
        val gameDirectory = gameSettings.gameDirectory ?: return
        val gameFile = gameDirectory.toVirtualFile(false) ?: return
        doUpdate(gameFile)
    }
    
    //org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater.doUpdate
    
    @Suppress("UnstableApiUsage")
    private fun doUpdate(gameFile: VirtualFile) {
        for(project in ProjectManager.getInstance().openProjects) {
            if(project.isDisposed) continue
            val isInProject = ProjectFileIndex.getInstance(project).isInContent(gameFile)
            if(!isInProject) continue
            val paradoxLibrary = project.paradoxLibrary
            val oldRoots = paradoxLibrary.roots
            val newRoots = paradoxLibrary.computeRoots()
            if(oldRoots == newRoots) continue
            paradoxLibrary.roots = newRoots
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
}