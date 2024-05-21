package icu.windea.pls.lang.tools.importer

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.model.*
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
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        val settings = tableModel.settings
        val gameType = settings.gameType.orDefault()
        val gameDataPath = Paths.getGameDataPath(gameType.title)?.toPathOrNull() ?: return
        if(!gameDataPath.exists()) {
            notifyWarning(settings, project, PlsBundle.message("mod.importer.error.gameDataDir", gameDataPath))
            return
        }
        val dbPath = getDbPath(gameDataPath)
        if(!dbPath.exists()) {
            notifyWarning(settings, project, PlsBundle.message("mod.importer.error.dbFile", dbPath))
            return
        }
        
        //IronyModManager.IO.Mods.Importers.ParadoxLauncherImporter.DatabaseImportAsync
        
        //Sqlite
        //connect jdbc:sqlite:~/Documents/Paradox Interactive/Stellaris/launcher-v2.sqlite
        
        //try {
        //    val collectionName = ""
        //    val count = 0
        //    val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
        //    finishImport(newSettingsList, tableView, tableModel)
        //    notify(settings, project, PlsBundle.message("mod.importer.info", collectionName, count))
        //} catch(e: Exception) {
        //    if(e is ProcessCanceledException) throw e
        //    thisLogger().info(e)
        //    notifyWarning(settings, project, PlsBundle.message("mod.importer.error"))
        //}
    }
}