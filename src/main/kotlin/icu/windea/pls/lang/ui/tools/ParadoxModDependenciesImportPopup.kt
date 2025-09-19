package icu.windea.pls.lang.ui.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.ep.tools.ParadoxDlcLoadImporter
import icu.windea.pls.ep.tools.ParadoxModImporter
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.Icon
import kotlin.io.path.exists

class ParadoxModDependenciesImportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable
) : BaseListPopupStep<ParadoxModImporter>(getTitle(), *getValues()) {
    companion object {
        private const val playlistsName = "playlists"
        private const val dlcLoadJsonName = "dlc_load.json"

        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.import.popup.title")

        private fun getValues() = ParadoxModImporter.EP_NAME.extensions
    }

    override fun getIconFor(value: ParadoxModImporter): Icon? = value.icon

    override fun getTextFor(value: ParadoxModImporter): String = value.text

    override fun isSpeedSearchEnabled(): Boolean = true

    override fun onChosen(selectedValue: ParadoxModImporter, finalChoice: Boolean) = doFinalStep {
        if (!selectedValue.isAvailable()) {
            PlsCoreManager.createNotification(NotificationType.WARNING, table.model.settings.qualifiedName, PlsBundle.message("mod.importer.error")).notify(project)
            return@doFinalStep
        }
        val settings = table.model.settings
        val qualifiedName = settings.qualifiedName
        val gameType = settings.finalGameType
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)

        // 选择或定位数据源（JSON/SQLite）
        var importData: icu.windea.pls.ep.tools.model.ParadoxModImportData? = null
        when (selectedValue) {
            is ParadoxModImporter.JsonBased -> {
                val jsonPath: Path? = if (selectedValue is ParadoxDlcLoadImporter) {
                    val p = gameDataPath?.resolve(dlcLoadJsonName)
                    if (p == null || !p.exists()) {
                        PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.file", p?.toString().orEmpty())).notify(project)
                        return@doFinalStep
                    }
                    p
                } else {
                    val defaultSelectedDir = gameDataPath?.resolve(playlistsName)
                    val defaultSelected = defaultSelectedDir?.toVirtualFile(false)
                    val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
                        .withTitle(PlsBundle.message("mod.importer.launcherJson.title"))
                        .apply { putUserData(PlsDataKeys.gameType, gameType) }
                    val file = FileChooser.chooseFile(descriptor, table, project, defaultSelected) ?: return@doFinalStep
                    Paths.get(file.path)
                }
                importData = selectedValue.importFromJson(jsonPath!!)
            }
            is ParadoxModImporter.SqliteBased -> {
                val p = gameDataPath?.let { selectedValue.defaultDbPath(it) }
                if (p == null || !p.exists()) {
                    PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.file", p?.toString().orEmpty())).notify(project)
                    return@doFinalStep
                }
                importData = selectedValue.importFromDatabase(p)
            }
        }

        try {
            val result = importData!!
            // 若包含游戏ID，与当前不一致则提示
            if (result.gameId != null && result.gameId != gameType.id) {
                PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.gameType")).notify(project)
                return@doFinalStep
            }

            // 将导入的模组信息转换为表模型条目
            var count = 0
            val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()

            // Steam Workshop 基础路径（用于 V3 导入映射 steamId -> 目录）
            val workshopDirPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)

            for (m in result.mods) {
                val modDirPath: Path? = when {
                    m.modDirectory != null -> m.modDirectory
                    m.source == ParadoxModSource.Steam && m.remoteId != null && workshopDirPath != null -> workshopDirPath.resolve(m.remoteId)
                    selectedValue is ParadoxDlcLoadImporter && m.name != null && m.name.endsWith(".mod", true) && gameDataPath != null -> {
                        val descriptorPath = gameDataPath.resolve(m.name)
                        val descriptorVf = descriptorPath.toVirtualFile(true)
                        val info = descriptorVf?.let { ParadoxMetadataManager.getModDescriptorInfo(it) }
                        info?.path?.let { Paths.get(it) }
                    }
                    else -> null
                }
                if (modDirPath == null || !modDirPath.exists()) continue
                val modVf = modDirPath.toVirtualFile(true) ?: continue
                val rootInfo = modVf.rootInfo
                if (rootInfo == null) continue // 要求存在模组描述符
                val modPath = modVf.path
                if (!table.model.modDependencyDirectories.add(modPath)) continue // 忽略已有
                count++
                val newSettings = ParadoxModDependencySettingsState().apply {
                    enabled = m.enabled
                    modDirectory = modPath
                }
                newSettingsList.add(newSettings)
            }

            // 插入到表格（若最后一个是当前模组，则插入到其前）
            val isCurrentAtLast = table.model.isCurrentAtLast()
            val position = if (isCurrentAtLast) table.model.rowCount - 1 else table.model.rowCount
            table.model.insertRows(position, newSettingsList)
            if (newSettingsList.isNotEmpty()) {
                table.setRowSelectionInterval(position, position + newSettingsList.size - 1)
            }

            val collectionName = result.collectionName ?: ""
            PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, PlsBundle.message("mod.importer.info", collectionName, count)).notify(project)
        } catch (_: Exception) {
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error")).notify(project)
        }
    }
}
