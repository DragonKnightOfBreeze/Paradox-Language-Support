package icu.windea.pls.lang.listeners

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.EditorNotifications
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.ParadoxLibrary
import icu.windea.pls.lang.ParadoxLibraryProvider
import icu.windea.pls.lang.editor.ParadoxGameDirectoryNotConfiguredEditorNotificationProvider
import icu.windea.pls.lang.paradoxLibrary
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.util.PlsAnalyzeManager

/**
 * 当添加或更改模组配置后，刷新库信息。
 *
 * @see ParadoxLibrary
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnModSettingsChangedListener : ParadoxModSettingsListener {
    override fun onAdd(modSettings: ParadoxModSettingsState) {
        doUpdate(modSettings.modDirectory)
    }

    override fun onChange(modSettings: ParadoxModSettingsState) {
        doUpdate(modSettings.modDirectory)
    }

    // org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater.doUpdate

    private fun doUpdate(directory: String?) {
        val root = directory?.orNull()?.toVirtualFile(false) ?: return
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(root) }
            if (!isInProject) continue
            val paradoxLibrary = project.paradoxLibrary
            paradoxLibrary.refreshRootsAsync()
        }

        // 重新解析根目录下已打开的文件（IDE之后会自动请求重新索引）
        val openedFiles = PlsAnalyzeManager.findOpenedFiles(onlyParadoxFiles = true).filter { VfsUtil.isAncestor(root, it, true) }
        PlsAnalyzeManager.reparseFiles(openedFiles)
    }
}

/**
 * 当更改模组配置后，更新编辑器通知。
 *
 * @see ParadoxGameDirectoryNotConfiguredEditorNotificationProvider
 */
class ParadoxUpdateEditorNotificationsOnModSettingsChangedListener : ParadoxModSettingsListener {
    override fun onAdd(modSettings: ParadoxModSettingsState) {
        EditorNotifications.updateAll()
    }

    override fun onChange(modSettings: ParadoxModSettingsState) {
        EditorNotifications.updateAll()
    }
}
