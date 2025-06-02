package icu.windea.pls.lang.editor

import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.*
import icu.windea.pls.tools.ui.*
import java.util.function.Function
import javax.swing.*

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
                ParadoxGameType.entries.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                val defaultList = defaultGameDirectories.toMutableEntryList()
                var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                val dialog = DefaultGameDirectoriesDialog(list)
                if (dialog.showAndGet()) {
                    list = dialog.resultList
                    val oldDefaultGameDirectories = defaultGameDirectories.toMutableMap()
                    val newDefaultGameDirectories = list.toMutableMap()
                    if (oldDefaultGameDirectories == newDefaultGameDirectories) return@action
                    settings.defaultGameDirectories = newDefaultGameDirectories
                    val messageBus = ApplicationManager.getApplication().messageBus
                    messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC).onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
                }
            }
            panel
        }
    }
}
