package icu.windea.pls.lang.tools.importer

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.lang.tools.model.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

private const val dlcLoadJsonPath = "dlc_load.json"
private const val collectionName = "Paradox"

/**
 * 从游戏数据目录下的配置文件`dlc_load.json`导入模组配置。
 *
 * See: [ParadoxImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxImporter.cs)
 */
class ParadoxFromGameImporter : ParadoxModImporter {
    override val text: String = PlsBundle.message("mod.importer.game")

    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        val settings = tableModel.settings
        val gameType = settings.gameType.orDefault()
        val gameDataPath = getDataProvider().getGameDataPath(gameType.title)?.toPathOrNull() ?: return
        if (!gameDataPath.exists()) {
            notifyWarning(settings, project, PlsBundle.message("mod.importer.error.gameDataDir", gameDataPath))
            return
        }
        val jsonPath = gameDataPath.resolve(dlcLoadJsonPath) ?: return
        if (!jsonPath.exists()) {
            notifyWarning(settings, project, PlsBundle.message("mod.importer.error.file", jsonPath))
            return
        }
        val file = jsonPath.toVirtualFile(true) ?: return
        try {
            val data = jsonMapper.readValue<ParadoxDlcLoadJson>(file.inputStream)

            var count = 0
            val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
            for (mod in data.enabledMods) {
                val descriptorPath = gameDataPath.resolve(mod)
                if (!descriptorPath.exists()) continue
                val descriptorFile = descriptorPath.toVirtualFile(true) ?: continue
                val descriptorInfo = ParadoxCoreManager.getDescriptorInfo(descriptorFile) ?: continue
                val modPath = descriptorInfo.path ?: continue
                val modDir = modPath.toVirtualFile() ?: continue
                val rootInfo = modDir.rootInfo
                if (rootInfo == null) continue //NOTE 目前要求这里的模组目录下必须有模组描述符文件
                count++
                if (!tableModel.modDependencyDirectories.add(modPath)) continue //忽略已有的
                val newSettings = ParadoxModDependencySettingsState()
                newSettings.enabled = true
                newSettings.modDirectory = modPath
                newSettingsList.add(newSettings)
            }

            //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
            val isCurrentAtLast = tableModel.isCurrentAtLast()
            val position = if (isCurrentAtLast) tableModel.rowCount - 1 else tableModel.rowCount
            tableModel.insertRows(position, newSettingsList)
            //选中刚刚添加的所有模组依赖
            tableView.setRowSelectionInterval(position, position + newSettingsList.size - 1)

            notify(settings, project, PlsBundle.message("mod.importer.info", collectionName, count))
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().info(e)
            notifyWarning(settings, project, PlsBundle.message("mod.importer.error"))
        }
    }
}
