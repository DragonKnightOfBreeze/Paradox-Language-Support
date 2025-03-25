package icu.windea.pls.lang.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.editor.*
import icu.windea.pls.lang.settings.*

/**
 * 当添加或更改模组配置后，刷新库信息。
 *
 * @see ParadoxLibrary
 * @see ParadoxLibraryProvider
 */
class ParadoxUpdateLibraryOnModSettingsChangedListener : ParadoxModSettingsListener {
    override fun onAdd(modSettings: ParadoxModSettingsState) {
        doUpdateLibrary(modSettings.modDirectory)
    }

    override fun onChange(modSettings: ParadoxModSettingsState) {
        doUpdateLibrary(modSettings.modDirectory)
    }

    //org.jetbrains.kotlin.idea.core.script.ucache.ScriptClassRootsUpdater.doUpdate

    private fun doUpdateLibrary(directory: String?) {
        val root = directory?.orNull()?.toVirtualFile(false) ?: return
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(root) }
            if (!isInProject) continue
            val paradoxLibrary = project.paradoxLibrary
            paradoxLibrary.refreshRoots()
        }
    }
}

/**
 * 当更改模组配置后，更新编辑器通知。
 *
 * @see ParadoxGameDirectoryNotConfiguredEditorNotificationProvider
 */
class ParadoxUpdateEditorNotificationsOnModSettingsChangedListener : ParadoxModSettingsListener {
    override fun onAdd(modSettings: ParadoxModSettingsState) {

    }

    override fun onChange(modSettings: ParadoxModSettingsState) {
        EditorNotifications.updateAll()
    }
}
