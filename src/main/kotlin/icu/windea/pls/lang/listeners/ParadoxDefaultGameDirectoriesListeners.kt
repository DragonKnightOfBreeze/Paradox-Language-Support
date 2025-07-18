package icu.windea.pls.lang.listeners

import com.intellij.openapi.project.*
import com.intellij.ui.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.editor.*
import icu.windea.pls.lang.util.*

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
        doUpdate()
    }

    private fun doUpdate() {
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val library = project.paradoxLibrary
            library.refreshRoots()
        }

        //重新解析已打开的文件
        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.reparseFiles(openedFiles)
    }
}
