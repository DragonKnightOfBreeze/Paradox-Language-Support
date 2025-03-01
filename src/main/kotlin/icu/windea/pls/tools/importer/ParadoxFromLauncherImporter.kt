package icu.windea.pls.tools.importer

import com.intellij.notification.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.ui.*
import java.nio.file.*

private const val dbPath = "launcher-v2.sqlite"

/**
 * 从Paradox启动器的Sqlite数据库中导入模组配置。
 *
 * See: [ParadoxLauncherImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporter.cs)
 */
open class ParadoxFromLauncherImporter : ParadoxModImporter {
    val defaultSelected: VirtualFile? = null

    override val text: String = PlsBundle.message("mod.importer.launcher")

    open fun getDbPath(gameDataPath: Path): Path {
        return gameDataPath.resolve(dbPath)
    }

    //TODO DO NOT implement this feature since sqlite jar is too large (12M+)

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val gameType = settings.gameType.orDefault()
        val gameDataPath = getDataProvider().getGameDataPath(gameType.title)?.toPathOrNull() ?: return
        if (!gameDataPath.exists()) {
            run {
                val title = settings.qualifiedName ?: return@run
                val message = PlsBundle.message("mod.importer.error.gameDataDir", gameDataPath)
                createNotification(title, message, NotificationType.WARNING).notify(project)
            }
            return
        }
        val dbPath = getDbPath(gameDataPath)
        if (!dbPath.exists()) {
            run {
                val title = settings.qualifiedName ?: return@run
                val content = PlsBundle.message("mod.importer.error.dbFile", dbPath)
                createNotification(title, content, NotificationType.WARNING).notify(project)
            }
            return
        }

        //IronyModManager.IO.Mods.Importers.ParadoxLauncherImporter.DatabaseImportAsync

        //Sqlite
        //connect jdbc:sqlite:~/Documents/Paradox Interactive/Stellaris/launcher-v2.sqlite

        //try {
        //    val collectionName = ""
        //    val count = 0
        //    val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
        //    finishImport(newSettingsList, table, table.model)
        //    notify(settings, project, PlsBundle.message("mod.importer.info", collectionName, count))
        //} catch(e: Exception) {
        //    if(e is ProcessCanceledException) throw e
        //    thisLogger().info(e)
        //    notifyWarning(settings, project, PlsBundle.message("mod.importer.error"))
        //}
    }
}
