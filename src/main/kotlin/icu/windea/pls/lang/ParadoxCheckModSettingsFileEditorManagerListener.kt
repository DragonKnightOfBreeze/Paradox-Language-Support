package icu.windea.pls.lang

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.util.concurrency.*
import icu.windea.pls.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.ui.*

/**
 * 当打开模组文件时，检查模组的游戏目录是否已配置。如果未配置则弹出通知。
 */
class ParadoxCheckModSettingsFileEditorManagerListener : FileEditorManagerListener {
    object Keys : KeyRegistry() {
        val checked by createKey<Boolean>(this)
    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        //For whole application (and never reset check status), not for each project

        val parent = file.parent ?: return
        if (parent.getUserData(Keys.checked) == true) return
        var dir = parent
        while (true) {
            dir = dir.parent ?: break
            if (dir.getUserData(Keys.checked) == true) {
                parent.putUserData(Keys.checked, true)
                return
            }
        }

        parent.putUserData(Keys.checked, true)

        //Slow operations are prohibited on EDT, so these code must be done in a background thread.

        ReadAction.nonBlocking<Boolean> action@{
            val project = source.project
            val fileInfo = file.fileInfo ?: return@action false
            val rootInfo = fileInfo.rootInfo
            if (rootInfo !is ParadoxRootInfo.Mod) return@action false
            val rootFile = rootInfo.rootFile
            if (!rootFile.isValid) return@action false

            val isInProject = ProjectFileIndex.getInstance(project).isInContent(rootFile)
            if (!isInProject) return@action false

            run {
                val modSettings = getProfilesSettings().modSettings.get(rootFile.path) ?: return@action false
                if (!modSettings.finalGameDirectory.isNullOrEmpty()) return@action false

                val title = modSettings.qualifiedName ?: return@action false
                val content = PlsBundle.message("mod.settings.notification.1.content")

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

                createNotification(title, content, NotificationType.INFORMATION)
                    .addAction(action1).addAction(action2).setImportant(true)
                    .notify(project)
            }

            true
        }.submit(AppExecutorUtil.getAppExecutorService())
    }
}
