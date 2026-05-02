package icu.windea.pls.model.analysis

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxLauncherSettingsJsonBasedGameMetadata(
    override val rootFile: VirtualFile,
    override val info: ParadoxLauncherSettingsJsonInfo,
    override val infoPath: String,
) : ParadoxRootMetadata.Game {
    override val name: String get() = gameType.title
    override val version: String? get() = info.rawVersion ?: info.version
    override val gameType: ParadoxGameType = computeGameType()

    private fun computeGameType(): ParadoxGameType {
        return ParadoxGameType.getAll().find { it.gameId == info.gameId } ?: throw IllegalStateException()
    }
}

data class ParadoxExecutableFileBasedGameMetadata(
    override val rootFile: VirtualFile,
    override val name: String,
    override val version: String?,
    override val gameType: ParadoxGameType,
) : ParadoxRootMetadata.Game {
    override val info: ParadoxRootMetadataInfo? get() = null
    override val infoPath: String? get() = null
}
