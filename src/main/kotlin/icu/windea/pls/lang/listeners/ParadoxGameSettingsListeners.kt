package icu.windea.pls.lang.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*

/**
 * 当添加或更改游戏配置后，刷新库信息。
 *
 * @see ParadoxLibrary
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnGameSettingsChangedListener : ParadoxGameSettingsListener {
    override fun onAdd(gameSettings: ParadoxGameSettingsState) {
        doUpdate(gameSettings.gameDirectory)
    }

    override fun onChange(gameSettings: ParadoxGameSettingsState) {
        doUpdate(gameSettings.gameDirectory)
    }

    //org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater.doUpdate

    private fun doUpdate(directory: String?) {
        val root = directory?.orNull()?.toVirtualFile(false) ?: return
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(root) }
            if (!isInProject) continue
            val paradoxLibrary = project.paradoxLibrary
            paradoxLibrary.refreshRoots()
        }

        //重新解析已打开的文件
        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.reparseFiles(openedFiles)
    }
}
