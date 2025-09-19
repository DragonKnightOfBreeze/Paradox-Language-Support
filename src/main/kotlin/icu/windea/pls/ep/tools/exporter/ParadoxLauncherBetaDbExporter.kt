package icu.windea.pls.ep.tools.exporter

import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.Constants

/**
 * 导出模组信息到官方启动器（Beta 版）的数据库文件（即游戏数据目录下的 `llauncher-v2_openbeta.sqlite`）。
 *
 * 参见：[ParadoxLauncherExporter202110.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter202110.cs)
 */
class ParadoxLauncherBetaDbExporter : ParadoxLauncherDbExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcherBeta")

    override fun getDbFileName() = Constants.launcherDbBetaPath
}
