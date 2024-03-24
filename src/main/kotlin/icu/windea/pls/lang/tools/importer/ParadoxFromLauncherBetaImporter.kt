package icu.windea.pls.lang.tools.importer

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
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