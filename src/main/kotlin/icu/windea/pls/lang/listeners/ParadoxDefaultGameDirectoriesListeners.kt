package icu.windea.pls.lang.listeners

import com.intellij.openapi.project.*
import com.intellij.ui.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.editor.*

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
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnDefaultGameDirectoriesChangedListener : ParadoxDefaultGameDirectoriesListener {
    override fun onChange(oldGameDirectories: Map<String, String>, newGameDirectories: Map<String, String>) {
        val directories = newGameDirectories.values.toMutableSet()
        directories.removeAll(oldGameDirectories.values.toSet())
        doUpdate(directories)
    }

    private fun doUpdate(directories: Set<String>) {
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val library = project.paradoxLibrary
            library.refreshRoots()
        }

        //重新解析已打开的文件
        val openedFiles = PlsManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsManager.reparseAndRefreshFiles(openedFiles)
    }
}
