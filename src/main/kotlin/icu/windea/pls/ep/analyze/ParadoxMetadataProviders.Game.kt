package icu.windea.pls.ep.analyze

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readBytes
import icu.windea.pls.lang.analyze.ParadoxMetadata
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.metadata.ParadoxLauncherSettingsJsonInfo

class ParadoxLauncherSettingsJsonBasedGameMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        // 尝试在根目录或其 `launcher` 子目录中查找 `launcher-settings.json`
        // 如果找到，再根据 `dlcPath` 的值获取游戏文件的根目录
        // 注意游戏文件的根目录可能是此目录的 `game` 子目录，而非此目录自身

        val infoFile = ParadoxMetadataManager.getLauncherSettingsJsonFile(rootFile) ?: return null
        val info = ParadoxMetadataManager.getLauncherSettingsJsonInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        override val rootFile: VirtualFile,
        override val infoFile: VirtualFile,
        val info: ParadoxLauncherSettingsJsonInfo,
    ) : ParadoxMetadata.Game {
        override val name: String get() = gameType.title
        override val version: String? get() = info.rawVersion ?: info.version
        override val gameType: ParadoxGameType = doGetGameType()

        private fun doGetGameType(): ParadoxGameType {
            return ParadoxGameType.getAll().find { it.gameId == info.gameId } ?: throw IllegalStateException()
        }
    }
}

class Eu5GameMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        // Europa Universalis V has no launcher metadata file.
        // So we detect the binary directly and read the version from the game branch file.
        rootFile.findFileByRelativePath("binaries/eu5.exe")?.exists() ?: return null
        val branch = rootFile.findFileByRelativePath("caesar_branch.txt")?.readBytes()?.toString(Charsets.UTF_8) ?: return null
        return Metadata(
            rootFile = rootFile,
            name = ParadoxGameType.Eu5.name,
            version = branch,
            gameType = ParadoxGameType.Eu5,
        )
    }

    class Metadata(
        override val rootFile: VirtualFile,
        override val name: String,
        override val version: String?,
        override val gameType: ParadoxGameType,
    ) : ParadoxMetadata.Game {
        override val infoFile: VirtualFile? get() = null
    }
}
