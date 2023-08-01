package icu.windea.pls.core.listeners

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.model.*

/**
 * 当打开项目中的模组文件时，检查模组的游戏目录是否已配置。如果未配置则弹出通知。
 */
class ParadoxCheckModSettingsFileEditorManagerListener : FileEditorManagerListener {
    companion object {
        val modPathsKey = Key.create<MutableSet<String>>("paradox.modSettings.check.modPaths")
    }
    
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        val project = source.project
        val fileInfo = file.fileInfo ?: return
        val rootInfo = fileInfo.rootInfo
        val rootFile = rootInfo.rootFile
        if(rootInfo !is ParadoxModRootInfo) return
        val modPaths = project.getOrPutUserData(modPathsKey) { mutableSetOf() }
        val modPath = rootFile.path
        if(!rootFile.isValid) {
            modPaths.remove(modPath)
            return
        }
        val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(rootFile) }
        if(!isInProject) {
            modPaths.remove(modPath)
            return
        }
        if(modPaths.contains(modPath)) return
        val modSettings = getProfilesSettings().modSettings.get(modPath) ?: return
        if(modSettings.gameDirectory.isNullOrEmpty()) {
            val qualifiedName = modSettings.qualifiedName ?: return //should not be null
            val action = NotificationAction.createSimple(PlsBundle.message("mod.settings.notification.1.action.1")) {
                val dialog = ParadoxModSettingsDialog(project, modSettings)
                dialog.show()
            }
            NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                qualifiedName,
                PlsBundle.message("mod.settings.notification.1.content"),
                NotificationType.INFORMATION
            ).addAction(action).setImportant(true).notify(project)
        }
        modPaths.add(modPath)
    }
}