package icu.windea.pls.ep.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.ui.tools.ParadoxModDependenciesTable
import icu.windea.pls.lang.util.PlsCoreManager
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * 从Paradox启动器的Sqlite数据库中导入模组配置。
 *
 * See: [ParadoxLauncherImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporter.cs)
 */
open class ParadoxFromLauncherImporter : ParadoxModImporter {
    companion object {
        private const val dbPath = "launcher-v2.sqlite"
    }

    override val text: String = PlsBundle.message("mod.importer.launcher")

    open fun getDbPath(gameDataPath: Path): Path {
        return gameDataPath.resolve(dbPath)
    }

    // TODO DO NOT implement this feature since sqlite jar is too large (12M+)

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val qualifiedName = settings.qualifiedName
        val gameType = settings.gameType ?: return
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title) ?: return
        if (!gameDataPath.exists()) {
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.gameDataDir", gameDataPath)).notify(project)
            return
        }
        val dbPath = getDbPath(gameDataPath)
        if (!dbPath.exists()) {
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.importer.error.dbFile", dbPath)).notify(project)
            return
        }

        // IronyModManager.IO.Mods.Importers.ParadoxLauncherImporter.DatabaseImportAsync

        // Sqlite
        // connect jdbc:sqlite:~/Documents/Paradox Interactive/Stellaris/launcher-v2.sqlite

        // try {
        //    val collectionName = ""
        //    val count = 0
        //    val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
        //    finishImport(newSettingsList, table, table.model)
        //    notify(settings, project, PlsBundle.message("mod.importer.info", collectionName, count))
        // } catch(e: Exception) {
        //    if(e is ProcessCanceledException) throw e
        //    thisLogger().info(e)
        //    notifyWarning(settings, project, PlsBundle.message("mod.importer.error"))
        // }
    }
}
