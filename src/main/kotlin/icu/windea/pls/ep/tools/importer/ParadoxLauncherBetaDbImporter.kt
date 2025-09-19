package icu.windea.pls.ep.tools.importer

import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.Constants

/**
 * 从官方启动器（Beta 版）的数据库文件（即游戏数据目录下的 `launcher-v2.sqlite`）导入模组信息。
 *
 * 参见：[ParadoxLauncherImporterBeta.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporterBeta.cs)
 */
class ParadoxLauncherBetaDbImporter : ParadoxLauncherDbImporter() {
    override val text: String = PlsBundle.message("mod.importer.launcherBeta")

    override fun getDbFileName() = Constants.launcherDbBetaPath
}
