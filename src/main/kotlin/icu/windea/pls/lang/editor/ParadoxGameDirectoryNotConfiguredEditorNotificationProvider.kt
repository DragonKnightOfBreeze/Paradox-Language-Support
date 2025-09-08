package icu.windea.pls.lang.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.util.application
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.listeners.ParadoxDefaultGameDirectoriesListener
import icu.windea.pls.lang.settings.DefaultGameDirectoriesDialog
import icu.windea.pls.lang.settings.finalGameDirectory
import icu.windea.pls.lang.ui.tools.ParadoxModSettingsDialog
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.util.function.Function
import javax.swing.JComponent

/**
 * 如果游戏目录未配置，则为模组文件提供通知，以便快速配置。仅适用于项目中的文本文件。
 */
class ParadoxGameDirectoryNotConfiguredEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val rootFile = rootInfo.rootFile
        if (!rootFile.isValid) return null

        val isInProject = ProjectFileIndex.getInstance(project).isInContent(rootFile)
        if (!isInProject) return null

        val modSettings = PlsFacade.getProfilesSettings().modSettings.get(rootFile.path) ?: return null
        if (modSettings.finalGameDirectory.isNotNullOrEmpty()) return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val message = PlsBundle.message("editor.notification.1.text")
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning).text(message)
            panel.createActionLabel(PlsBundle.message("editor.notification.1.action.1")) {
                val dialog = ParadoxModSettingsDialog(project, modSettings)
                dialog.show()
            }
            panel.createActionLabel(PlsBundle.message("editor.notification.1.action.2")) action@{
                val settings = PlsFacade.getSettings()
                val defaultGameDirectories = settings.defaultGameDirectories
                ParadoxGameType.getAll().forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                val defaultList = defaultGameDirectories.toMutableEntryList()
                var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                val dialog = DefaultGameDirectoriesDialog(list)
                if (dialog.showAndGet()) {
                    list = dialog.resultList
                    val oldDefaultGameDirectories = defaultGameDirectories.toMutableMap()
                    val newDefaultGameDirectories = list.toMutableMap()
                    if (oldDefaultGameDirectories == newDefaultGameDirectories) return@action
                    settings.defaultGameDirectories = newDefaultGameDirectories
                    application.messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC).onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
                }
            }
            panel
        }
    }
}
