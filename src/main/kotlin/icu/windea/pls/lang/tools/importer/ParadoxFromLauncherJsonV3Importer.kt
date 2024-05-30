package icu.windea.pls.lang.tools.importer

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.lang.tools.model.*
import icu.windea.pls.model.*

/**
 * 从启动器JSON配置文件导入模组配置。
 */
class ParadoxFromLauncherJsonV3Importer : ParadoxModImporter {
    var defaultSelected: VirtualFile? = null
    
    override val text: String = PlsBundle.message("mod.importer.launcherJson")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        val settings = tableModel.settings
        val gameType = settings.gameType.orDefault()
        if(defaultSelected == null) {
            val gameDataPath = PathProvider.getGameDataPath(gameType.title)?.toPathOrNull()
            val playlistsPath = gameDataPath?.resolve("playlists")
            val playlistsFile = playlistsPath?.toVirtualFile(false)
            if(playlistsFile != null) defaultSelected = playlistsFile
        }
        val workshopDirPath = PathProvider.getSteamWorkshopPath(gameType.steamId)?.toPathOrNull() ?: return
        if(!workshopDirPath.exists()) {
            notifyWarning(settings, project, PlsBundle.message("mod.importer.error.steamWorkshopDir", workshopDirPath))
            return
        }
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            .withTitle(PlsBundle.message("mod.importer.launcherJson.title"))
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        FileChooser.chooseFile(descriptor, project, tableView, defaultSelected) { file ->
            try {
                val data = jsonMapper.readValue<ParadoxLauncherJsonV3>(file.inputStream)
                if(data.game != gameType.id) {
                    notifyWarning(settings, project, PlsBundle.message("mod.importer.error.gameType"))
                    return@chooseFile
                }
                
                val collectionName = data.name
                var count = 0
                val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
                for(mod in data.mods.sortedBy { it.position }) {
                    val modSteamId = mod.steamId ?: continue
                    val path = workshopDirPath.resolve(modSteamId)
                    if(!path.exists()) continue
                    val modDir = path.toVirtualFile(true) ?: continue
                    val rootInfo = modDir.rootInfo
                    if(rootInfo == null) continue //NOTE 目前要求这里的模组目录下必须有模组描述符文件
                    val modPath = modDir.path
                    count++
                    if(!tableModel.modDependencyDirectories.add(modPath)) continue //忽略已有的
                    val newSettings = ParadoxModDependencySettingsState()
                    newSettings.enabled = mod.enabled
                    newSettings.modDirectory = modPath
                    newSettingsList.add(newSettings)
                }
                
                //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
                val isCurrentAtLast = tableModel.isCurrentAtLast()
                val position = if(isCurrentAtLast) tableModel.rowCount - 1 else tableModel.rowCount
                tableModel.insertRows(position, newSettingsList)
                //选中刚刚添加的所有模组依赖
                tableView.setRowSelectionInterval(position, position + newSettingsList.size - 1)
                
                notify(settings, project, PlsBundle.message("mod.importer.info", collectionName, count))
            } catch(e: Exception) {
                if(e is ProcessCanceledException) throw e
                thisLogger().info(e)
                notifyWarning(settings, project, PlsBundle.message("mod.importer.error"))
            }
        }
    }
}