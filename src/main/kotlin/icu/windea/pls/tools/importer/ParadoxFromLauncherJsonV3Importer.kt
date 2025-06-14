package icu.windea.pls.tools.importer

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.notification.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.model.*
import icu.windea.pls.tools.ui.*
import kotlin.io.path.*

/**
 * 从启动器JSON配置文件导入模组配置。
 */
class ParadoxFromLauncherJsonV3Importer : ParadoxModImporter {
    var defaultSelected: VirtualFile? = null

    override val text: String = PlsBundle.message("mod.importer.launcherJson")

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val gameType = settings.gameType.orDefault()
        if (defaultSelected == null) {
            val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
            val playlistsPath = gameDataPath?.resolve("playlists")
            val playlistsFile = playlistsPath?.toVirtualFile(false)
            if (playlistsFile != null) defaultSelected = playlistsFile
        }
        val workshopDirPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId) ?: return
        if (!workshopDirPath.exists()) {
            run {
                val title = settings.qualifiedName ?: return@run
                val content = PlsBundle.message("mod.importer.error.steamWorkshopDir", workshopDirPath)
                createNotification(title, content, NotificationType.WARNING).notify(project)
            }
            return
        }
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            .withTitle(PlsBundle.message("mod.importer.launcherJson.title"))
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        FileChooser.chooseFile(descriptor, project, table, defaultSelected) { file ->
            try {
                val data = jsonMapper.readValue<ParadoxLauncherJsonV3>(file.inputStream)
                if (data.game != gameType.id) {
                    run {
                        val title = settings.qualifiedName ?: return@run
                        val content = PlsBundle.message("mod.importer.error.gameType")
                        createNotification(title, content, NotificationType.WARNING).notify(project)
                    }
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

                run {
                    val title = settings.qualifiedName ?: return@run
                    val content = PlsBundle.message("mod.importer.info", collectionName, count)
                    createNotification(title, content, NotificationType.INFORMATION).notify(project)
                }
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)

                run {
                    val title = settings.qualifiedName ?: return@run
                    val content = PlsBundle.message("mod.importer.error")
                    createNotification(title, content, NotificationType.WARNING).notify(project)
                }
            }
        }
    }
}
