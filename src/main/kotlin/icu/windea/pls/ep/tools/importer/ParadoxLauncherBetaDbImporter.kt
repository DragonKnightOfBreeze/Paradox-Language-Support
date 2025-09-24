package icu.windea.pls.ep.tools.importer

import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.Constants

/**
 * 从官方启动器（Beta 版）的数据库文件导入模组信息。
 *
 * 数据文件默认为游戏数据目录下的 `launcher-v2_openbeta.sqlite`。
 *
 * 来自已激活的播放集，或者任意一个播放集。
 *
 * 参见：[ParadoxLauncherImporterBeta.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporterBeta.cs)
 */
class ParadoxLauncherBetaDbImporter : ParadoxLauncherDbImporter() {
    override val text: String = PlsBundle.message("mod.importer.launcherBeta")

    override fun getDbFileName() = Constants.launcherDbBetaPath
}
