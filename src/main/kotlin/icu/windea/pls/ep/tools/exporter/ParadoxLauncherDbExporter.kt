package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path

/**
 * 导出模组信息到官方启动器的数据库文件。
 *
 * 数据文件默认为游戏数据目录下的 `launcher-v2.sqlite`。
 *
 * 参见：[ParadoxLauncherExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter.cs)
 */
open class ParadoxLauncherDbExporter : ParadoxDbBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcher")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        TODO("Not yet implemented")
    }

    override fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor {
        TODO("Not yet implemented")
    }

    override fun getSavedBaseDir(gameType: ParadoxGameType): Path? {
        TODO("Not yet implemented")
    }

    override fun getSavedFileName(gameType: ParadoxGameType): String? {
        TODO("Not yet implemented")
    }

    protected open fun getDbFileName() = Constants.launcherDbPath
}
