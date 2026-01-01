package icu.windea.pls.lang.listeners

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtil
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.ParadoxLibrary
import icu.windea.pls.lang.ParadoxLibraryService
import icu.windea.pls.lang.settings.ParadoxGameSettingsState
import icu.windea.pls.lang.util.PlsAnalyzeManager

/**
 * 当添加或更改游戏配置后，刷新库信息。
 *
 * @see ParadoxLibrary
 */
class ParadoxUpdateLibraryOnGameSettingsChangedListener : ParadoxGameSettingsListener {
    override fun onAdd(gameSettings: ParadoxGameSettingsState) {
        doUpdate(gameSettings.gameDirectory)
    }

    override fun onChange(gameSettings: ParadoxGameSettingsState) {
        doUpdate(gameSettings.gameDirectory)
    }

    // org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater.doUpdate

    private fun doUpdate(directory: String?) {
        val root = directory?.orNull()?.toVirtualFile() ?: return
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(root) }
            if (!isInProject) continue
            ParadoxLibraryService.getInstance(project).refreshRootsAsync()
        }

        // 重新解析根目录下已打开的文件（IDE之后会自动请求重新索引）
        val files = PlsAnalyzeManager.findOpenedFiles(onlyParadoxFiles = true).filter { VfsUtil.isAncestor(root, it, true) }
        PlsAnalyzeManager.reparseFiles(files)
    }
}
