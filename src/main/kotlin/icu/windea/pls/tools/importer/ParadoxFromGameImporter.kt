package icu.windea.pls.tools.importer

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.module.kotlin.*
import com.intellij.notification.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.ui.*
import kotlin.io.path.*

private const val dlcLoadJsonPath = "dlc_load.json"
private const val collectionName = "Paradox"

/**
 * 从游戏数据目录下的配置文件`dlc_load.json`导入模组配置。
 *
 * See: [ParadoxImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxImporter.cs)
 */
class ParadoxFromGameImporter : ParadoxModImporter {
    override val text: String = PlsBundle.message("mod.importer.game")

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val gameType = settings.gameType.orDefault()
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title) ?: return
        if (!gameDataPath.exists()) {
            run {
                val title = settings.qualifiedName ?: return@run
                val content = PlsBundle.message("mod.importer.error.gameDataDir", gameDataPath)
                createNotification(title, content, NotificationType.WARNING).notify(project)
            }
            return
        }
        val jsonPath = gameDataPath.resolve(dlcLoadJsonPath) ?: return
        if (!jsonPath.exists()) {
            run {
                val title = settings.qualifiedName ?: return@run
                val content = PlsBundle.message("mod.importer.error.file", jsonPath)
                createNotification(title, content, NotificationType.WARNING).notify(project)
            }
            return
        }
        val file = jsonPath.toVirtualFile(true) ?: return
        try {
            val data = jsonMapper.readValue<DlcLoad>(file.inputStream)

            var count = 0
            val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
            for (mod in data.enabledMods) {
                val descriptorPath = gameDataPath.resolve(mod)
                if (!descriptorPath.exists()) continue
                val descriptorFile = descriptorPath.toVirtualFile(true) ?: continue
                val descriptorInfo = ParadoxMetadataManager.getModDescriptorInfo(descriptorFile) ?: continue
                val modPath = descriptorInfo.path ?: continue
                val modDir = modPath.toVirtualFile() ?: continue
                val rootInfo = modDir.rootInfo
                if (rootInfo == null) continue //NOTE 目前要求这里的模组目录下必须有模组描述符文件
                count++
                if (!table.model.modDependencyDirectories.add(modPath)) continue //忽略已有的
                val newSettings = ParadoxModDependencySettingsState()
                newSettings.enabled = true
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

    data class DlcLoad(
        @JsonProperty("disabled_dlcs")
        val disabledDlcs: List<String>,
        @JsonProperty("enabled_mods")
        val enabledMods: List<String>
    )
}
