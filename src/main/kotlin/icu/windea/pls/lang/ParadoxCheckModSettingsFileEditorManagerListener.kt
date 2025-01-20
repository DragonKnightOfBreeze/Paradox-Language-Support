package icu.windea.pls.lang

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.ui.*

/**
 * 当打开项目中的模组文件时，检查模组的游戏目录是否已配置。如果未配置则弹出通知。
 */
class ParadoxCheckModSettingsFileEditorManagerListener : FileEditorManagerListener {
    object Keys : KeyRegistry() {
        val checkedModPaths by createKey<MutableSet<String>>(this)
    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        val project = source.project
        val fileInfo = file.fileInfo ?: return
        val rootInfo = fileInfo.rootInfo
        val rootFile = rootInfo.rootFile
        if (rootInfo !is ParadoxModRootInfo) return
        val modPaths = project.getOrPutUserData(Keys.checkedModPaths) { mutableSetOf() }
        val modPath = rootFile.path
        if (!rootFile.isValid) {
            modPaths.remove(modPath)
            return
        }
        val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(rootFile) }
        if (!isInProject) {
            modPaths.remove(modPath)
            return
        }
        if (modPaths.contains(modPath)) return
        val modSettings = getProfilesSettings().modSettings.get(modPath) ?: return
        if (modSettings.finalGameDirectory.isNullOrEmpty()) {
            val qualifiedName = modSettings.qualifiedName ?: return

            //打开模组的配置页面
            val action1 = NotificationAction.createSimple(PlsBundle.message("mod.settings.notification.1.action.1")) {
                val dialog = ParadoxModSettingsDialog(project, modSettings)
                dialog.show()
            }
            //打开默认游戏目录的配置页面
            val action2 = NotificationAction.createSimple(PlsBundle.message("mod.settings.notification.1.action.2")) {
                val settings = getSettings()
                val oldDefaultGameDirectories = settings.defaultGameDirectories
                ParadoxGameType.entries.forEach { oldDefaultGameDirectories.putIfAbsent(it.id, "") }
                val defaultList = oldDefaultGameDirectories.toMutableEntryList()
                var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                val dialog = ParadoxGameDirectoriesDialog(list)
                if (dialog.showAndGet()) {
                    list = dialog.resultList
                    settings.defaultGameDirectories = list.toMutableMap()
                    if (oldDefaultGameDirectories != settings.defaultGameDirectories) {
                        val messageBus = ApplicationManager.getApplication().messageBus
                        messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC)
                            .onChange(oldDefaultGameDirectories, settings.defaultGameDirectories)
                    }
                }
            }

            run {
                val title = qualifiedName
                val content = PlsBundle.message("mod.settings.notification.1.content")
                createNotification(title, content, NotificationType.INFORMATION)
                    .addAction(action1).addAction(action2).setImportant(true)
                    .notify(project)
            }
        }
        modPaths.add(modPath)
    }
}
