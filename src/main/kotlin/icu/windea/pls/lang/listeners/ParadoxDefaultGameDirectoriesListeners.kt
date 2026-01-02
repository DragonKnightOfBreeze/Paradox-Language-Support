package icu.windea.pls.lang.listeners

import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.EditorNotifications
import icu.windea.pls.lang.ParadoxLibrary
import icu.windea.pls.lang.ParadoxLibraryService
import icu.windea.pls.lang.editor.ParadoxGameDirectoryNotConfiguredEditorNotificationProvider
import icu.windea.pls.lang.util.PlsDaemonManager

/**
 * 当更改默认游戏目录映射后，需要更新编辑器通知。
 *
 * @see ParadoxGameDirectoryNotConfiguredEditorNotificationProvider
 */
class ParadoxUpdateEditorNotificationsOnDefaultGameDirectoriesChangedListener : ParadoxDefaultGameDirectoriesListener {
    override fun onChange(oldGameDirectories: Map<String, String>, newGameDirectories: Map<String, String>) {
        EditorNotifications.updateAll()
    }
}

/**
 * 当更改默认游戏目录映射后，刷新库信息。
 *
 * @see ParadoxLibrary
 */
class ParadoxUpdateLibraryOnDefaultGameDirectoriesChangedListener : ParadoxDefaultGameDirectoriesListener {
    override fun onChange(oldGameDirectories: Map<String, String>, newGameDirectories: Map<String, String>) {
        val directories = newGameDirectories.values.toMutableSet()
        directories.removeAll(oldGameDirectories.values.toSet())
        doUpdate()
    }

    private fun doUpdate() {
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            ParadoxLibraryService.getInstance(project).refreshRootsAsync()
        }

        // 重新解析已打开的文件（IDE之后会自动请求重新索引）
        val files = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsDaemonManager.reparseFiles(files)
    }
}
