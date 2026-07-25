package icu.windea.pls.ep.analysis

import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.lang.analysis.ParadoxRootMetadataUtil
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.analysis.ParadoxExecutableFileBasedGameMetadata
import icu.windea.pls.model.analysis.ParadoxLauncherSettingsJsonBasedGameMetadata
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import java.nio.file.Path

class ParadoxLauncherSettingsJsonBasedGameMetadataProvider : ParadoxRootMetadataProvider {
    override fun getRootMetadata(rootPath: Path): ParadoxRootMetadata? {
        // 尝试在根目录或其 `launcher` 子目录中查找 `launcher-settings.json`
        // 如果找到，再根据 `dlcPath` 的值获取游戏文件的根目录
        // 注意游戏文件的根目录可能是此目录的 `game` 子目录，而非此目录自身

        val infoPath = ParadoxRootMetadataUtil.getLauncherSettingsJsonPath(rootPath) ?: return null
        val info = ParadoxRootMetadataUtil.getLauncherSettingsJsonInfo(infoPath) ?: return null
        return ParadoxLauncherSettingsJsonBasedGameMetadata(rootPath, infoPath, info)
    }
}

class ParadoxExecutableFileBasedGameMetadataProvider : ParadoxRootMetadataProvider {
    private val allowedGameTypes = arrayOf(
        ParadoxGameType.Ck2,
        ParadoxGameType.Ck3,
        ParadoxGameType.Eu5,
        ParadoxGameType.Ir,
        ParadoxGameType.Vic2,
        ParadoxGameType.Vic3,
    )

    override fun getRootMetadata(rootPath: Path): ParadoxRootMetadata? {
        // 尝试根据 `executableBaseNames` 在根目录或者特定的子目录中查找游戏的可执行文件
        // 如果找到，再尝试查找游戏的分支标记文件，并尝试从中获取游戏版本信息
        // 排除直接的二进制文件目录（如 `binaries`）
        // #339 注意如果用 Proton 运行（许多 Linux 用户如此），Linux 系统上的游戏可执行文件也会使用 `.exe` 扩展名（Windows 二进制）

        if (ParadoxGameManager.isBinariesDirectoryPath(rootPath)) return null
        for (gameType in allowedGameTypes) {
            val executablePath = ParadoxGameManager.getExecutablePath(gameType, rootPath) ?: continue
            val branchPath = ParadoxGameManager.getBranchPath(gameType, rootPath)
            return ParadoxExecutableFileBasedGameMetadata(gameType, rootPath, executablePath, branchPath)
        }
        return null
    }
}
