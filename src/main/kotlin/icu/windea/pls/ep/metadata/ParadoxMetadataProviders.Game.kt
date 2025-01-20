package icu.windea.pls.ep.metadata

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxLauncherSettingsBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        //尝试从根目录向下查找launcher-settings.json，如果找到，再根据"dlcPath"的值获取游戏文件的根目录
        //注意游戏文件的根目录可能是此目录的game子目录，而非此目录自身
        val launcherSettingsFile = runReadAction { ParadoxMetadataManager.getLauncherSettingsFile(rootFile) } ?: return null
        val launcherSettingsInfo = ParadoxMetadataManager.getLauncherSettingsInfo(launcherSettingsFile) ?: return null
        return Metadata(rootFile, launcherSettingsFile, launcherSettingsInfo)
    }

    class Metadata(
        rootFile: VirtualFile,
        val launcherSettingsFile: VirtualFile,
        val launcherSettingsInfo: ParadoxLauncherSettingsInfo,
    ) : ParadoxMetadata {
        override val forGame: Boolean get() = true
        override val name: String get() = gameType.title
        override val version: String get() = launcherSettingsInfo.version
        override val inferredGameType: ParadoxGameType? get() = null
        override val gameType: ParadoxGameType = doGetGameType()
        override val rootFile: VirtualFile = rootFile
        override val entryFile: VirtualFile = doGetEntryFile()

        private fun doGetGameType(): ParadoxGameType {
            return ParadoxGameType.entries.find { it.gameId == launcherSettingsInfo.gameId } ?: throw IllegalStateException()
        }

        private fun doGetEntryFile(): VirtualFile {
            val dlcPath = launcherSettingsInfo.dlcPath
            val path = launcherSettingsFile.toNioPath().parent.resolve(dlcPath).normalize().toAbsolutePath()
            return VfsUtil.findFile(path, true) ?: throw IllegalStateException()
        }
    }
}
