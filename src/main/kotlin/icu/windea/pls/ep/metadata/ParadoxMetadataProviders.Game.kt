package icu.windea.pls.ep.metadata

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxLauncherSettingsBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        //尝试在根目录或其launcher子目录中查找launcher-settings.json
        //如果找到，再根据"dlcPath"的值获取游戏文件的根目录
        //注意游戏文件的根目录可能是此目录的game子目录，而非此目录自身
        val infoFile = runReadAction { ParadoxMetadataManager.getLauncherSettingsFile(rootFile) } ?: return null
        val info = ParadoxMetadataManager.getLauncherSettingsInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        rootFile: VirtualFile,
        val infoFile: VirtualFile,
        val info: ParadoxLauncherSettingsInfo,
    ) : ParadoxMetadata.Game {
        override val name: String get() = gameType.title
        override val version: String get() = info.version
        override val inferredGameType: ParadoxGameType? get() = null
        override val gameType: ParadoxGameType = doGetGameType()
        override val rootFile: VirtualFile = rootFile
        override val entryFile: VirtualFile = doGetEntryFile()

        private fun doGetGameType(): ParadoxGameType {
            return ParadoxGameType.entries.find { it.gameId == info.gameId } ?: throw IllegalStateException()
        }

        private fun doGetEntryFile(): VirtualFile {
            val dlcPath = info.dlcPath
            val path = infoFile.toNioPath().parent.resolve(dlcPath).normalize().toAbsolutePath()
            return VfsUtil.findFile(path, true) ?: throw IllegalStateException()
        }
    }
}
