package icu.windea.pls.ep.tools

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.notification.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.ui.tools.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.tools.*
import kotlin.io.path.*

/**
 * 从启动器JSON配置文件导入模组配置。
 */
class ParadoxLauncherJsonV3Importer : ParadoxModImporter {
    companion object {
        private const val playlistsName = "playlists"
    }

    override val text: String = PlsBundle.message("mod.importer.launcherJson")

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val qualifiedName = settings.qualifiedName
        val gameType = settings.gameType ?: return
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        val defaultSelectedDir = gameDataPath?.resolve(playlistsName)
        val defaultSelected = defaultSelectedDir?.toVirtualFile(false)
        val workshopDirPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId) ?: return
        if (!workshopDirPath.exists()) {
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.steamWorkshopDir", workshopDirPath)).notify(project)
            return
        }
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            .withTitle(PlsBundle.message("mod.importer.launcherJson.title"))
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        FileChooser.chooseFile(descriptor, project, table, defaultSelected) { file ->
            try {
                val data = ObjectMappers.jsonMapper.readValue<ParadoxLauncherJsonV3>(file.inputStream)
                if (data.game != gameType.id) {
                    PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.gameType")).notify(project)
                    return@chooseFile
                }

                val collectionName = data.name
                var count = 0
                val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
                for (mod in data.mods.sortedBy { it.position }) {
                    val modSteamId = mod.steamId ?: continue
                    val path = workshopDirPath.resolve(modSteamId)
                    if (!path.exists()) continue
                    val modDir = path.toVirtualFile(true) ?: continue
                    val rootInfo = modDir.rootInfo
                    if (rootInfo == null) continue //NOTE 目前要求这里的模组目录下必须有模组描述符文件
                    val modPath = modDir.path
                    count++
                    if (!table.model.modDependencyDirectories.add(modPath)) continue //忽略已有的
                    val newSettings = ParadoxModDependencySettingsState()
                    newSettings.enabled = mod.enabled
                    newSettings.modDirectory = modPath
                    newSettingsList.add(newSettings)
                }

                //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
                val isCurrentAtLast = table.model.isCurrentAtLast()
                val position = if (isCurrentAtLast) table.model.rowCount - 1 else table.model.rowCount
                table.model.insertRows(position, newSettingsList)
                //选中刚刚添加的所有模组依赖
                table.setRowSelectionInterval(position, position + newSettingsList.size - 1)

                PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, PlsBundle.message("mod.importer.info", collectionName, count)).notify(project)
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)

                PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error")).notify(project)
            }
        }
    }
}
