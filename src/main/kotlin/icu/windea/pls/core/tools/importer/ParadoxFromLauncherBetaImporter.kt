package icu.windea.pls.core.tools.importer

import icu.windea.pls.*
import java.nio.file.*

private const val dbPath = "launcher-v2_openbeta.sqlite"

/**
 * 从Paradox启动器（Beta版）的Sqlite数据库中导入模组配置。
 *
 * See: [ParadoxLauncherImporterBeta.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporterBeta.cs)
 */
class ParadoxFromLauncherBetaImporter : ParadoxFromLauncherImporter() {
    override val text: String = PlsBundle.message("mod.importer.launcherBeta")
    
    override fun getDbPath(gameDataPath: Path): Path {
        return gameDataPath.resolve(dbPath)
    }
}