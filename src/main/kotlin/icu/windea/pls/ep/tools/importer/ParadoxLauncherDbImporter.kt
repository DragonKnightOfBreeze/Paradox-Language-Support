package icu.windea.pls.ep.tools.importer

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path

/**
 * 从官方启动器的数据库文件（即游戏数据目录下的 `launcher-v2.sqlite`）导入模组信息。
 *
 * 参见：[ParadoxLauncherImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporter.cs)
 */
open class ParadoxLauncherDbImporter : ParadoxDbBasedModImporter() {
    override val text: String = PlsBundle.message("mod.importer.launcher")

    // TODO DO NOT implement this feature since sqlite jar is too large (12M+)

    // override fun execute(project: Project, table: ParadoxModDependenciesTable) {
    //     val settings = table.model.settings
    //     val qualifiedName = settings.qualifiedName
    //     val gameType = settings.gameType ?: return
    //     val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title) ?: return
    //     if (!gameDataPath.exists()) {
    //         PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.gameDataDir", gameDataPath)).notify(project)
    //         return
    //     }
    //     val dbPath = getDbPath(gameDataPath)
    //     if (!dbPath.exists()) {
    //         PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.dbFile", dbPath)).notify(project)
    //         return
    //     }
    //
    //     // IronyModManager.IO.Mods.Importers.ParadoxLauncherImporter.DatabaseImportAsync
    //
    //     // Sqlite
    //     // connect jdbc:sqlite:~/Documents/Paradox Interactive/Stellaris/launcher-v2.sqlite
    //
    //     // try {
    //     //    val collectionName = ""
    //     //    val count = 0
    //     //    val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
    //     //    finishImport(newSettingsList, table, table.model)
    //     //    notify(settings, project, PlsBundle.message("mod.importer.info", collectionName, count))
    //     // } catch(e: Exception) {
    //     //    if(e is ProcessCanceledException) throw e
    //     //    thisLogger().info(e)
    //     //    notifyWarning(settings, project, PlsBundle.message("mod.importer.error"))
    //     // }
    // }

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModImporter.Result {
        TODO("Not yet implemented")
    }

    override fun createFileChooserDescriptor(gameType: ParadoxGameType): FileChooserDescriptor {
        TODO("Not yet implemented")
    }

    override fun getSelectedFile(gameType: ParadoxGameType): VirtualFile? {
        TODO("Not yet implemented")
    }

    protected open fun getDbFileName() = Constants.launcherDbPath
}
