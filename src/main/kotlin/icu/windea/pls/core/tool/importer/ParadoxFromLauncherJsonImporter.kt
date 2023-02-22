package icu.windea.pls.core.tool.importer

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.notification.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import java.nio.file.*

class ParadoxFromLauncherJsonImporter : ParadoxModDependenciesImporter {
    var defaultSelected: VirtualFile? = null
    
    override val text: String = PlsBundle.message("mod.importer.launcherJson")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        val settings = tableModel.settings
        val gameType = settings.gameType.orDefault()
        setDefaultSelected(gameType)
        val workshopDir = getSteamWorkshopPath(gameType.gameSteamId)
        if(workshopDir == null) {
            notifyWarning(settings, project, PlsBundle.message("mod.importer.launcherJson.error.3"))
            return
        }
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            .withTitle(PlsBundle.message("mod.importer.launcherJson.title"))
            .apply { putUserData(PlsDataKeys.gameTypeKey, gameType) }
        FileChooser.chooseFile(descriptor, project, tableView, defaultSelected) { file ->
            try {
                val data = jsonMapper.readValue<ParadoxLauncherJson>(file.inputStream)
                if(data.game != gameType.id) {
                    notifyWarning(settings, project, PlsBundle.message("mod.importer.launcherJson.error.2"))
                    return@chooseFile
                }
                
                var count = 0
                val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
                for(mod in data.mods.sortedBy { it.position }) {
                    val path = Path.of(workshopDir, mod.steamId)
                    if(!path.exists()) continue
                    val modDir = VfsUtil.findFile(path, false) ?: continue
                    val rootInfo = ParadoxCoreHandler.resolveRootInfo(modDir)
                    if(rootInfo == null) continue //目前要求导入的模组目录下必须有模组描述符文件
                    val modPath = modDir.path
                    count++
                    if(tableModel.modDependencyDirectories.contains(modPath)) continue //忽略已有的
                    val newSettings = ParadoxModDependencySettingsState()
                    newSettings.enabled = mod.enabled
                    newSettings.modDirectory = modPath
                    newSettingsList.add(newSettings)
                }
                
                //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
                val rowCount = tableModel.rowCount
                tableModel.addRows(newSettingsList)
                fun ensureCurrentAtLast() {
                    if(rowCount == tableModel.rowCount) return
                    val currentModDirectory = settings.castOrNull<ParadoxModSettingsState>()?.modDirectory
                    if(currentModDirectory == null) return
                    val lastRow = tableModel.getItem(rowCount - 1)
                    val lastModDirectory = lastRow.modDirectory
                    if(currentModDirectory != lastModDirectory) return
                    tableModel.removeRow(rowCount - 1)
                    tableModel.addRow(lastRow)
                }
                ensureCurrentAtLast()
                
                notify(settings, project, PlsBundle.message("mod.importer.launcherJson.info", data.name, count))
            } catch(e: Exception) {
                thisLogger().info(e)
                notifyWarning(settings, project, PlsBundle.message("mod.importer.launcherJson.error.1"))
            }
        }
    }
    
    private fun setDefaultSelected(gameType: ParadoxGameType) {
        if(defaultSelected != null) return
        val path = (getGameDataPath(gameType.name) + "/playlists").toPathOrNull() ?: return
        defaultSelected = VfsUtil.findFile(path, false)
    }
    
    fun notify(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
        NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
            settings.qualifiedName,
            message,
            NotificationType.INFORMATION
        ).notify(project)
    }
    
    fun notifyWarning(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
        NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
            settings.qualifiedName,
            message,
            NotificationType.WARNING
        ).notify(project)
    }
}