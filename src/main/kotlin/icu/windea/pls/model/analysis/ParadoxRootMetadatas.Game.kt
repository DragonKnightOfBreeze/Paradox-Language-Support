package icu.windea.pls.model.analysis

import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path
import kotlin.io.path.useLines

data class ParadoxLauncherSettingsJsonBasedGameMetadata(
    override val rootPath: Path,
    override val infoPath: Path,
    override val info: ParadoxLauncherSettingsJsonInfo,
) : ParadoxRootMetadata.Game {
    override val name: String get() = gameType.title
    override val version: String? get() = info.rawVersion ?: info.version
    override val gameType: ParadoxGameType = computeGameType()
    override val infoPresentablePath: String = rootPath.relativize(infoPath).toString().normalizePath()

    private fun computeGameType(): ParadoxGameType {
        return ParadoxGameType.getAll().find { it.gameId == info.gameId } ?: throw IllegalStateException()
    }
}

data class ParadoxExecutableFileBasedGameMetadata(
    override val gameType: ParadoxGameType,
    override val rootPath: Path,
    val executablePath: Path,
    val branchPath: Path?,
) : ParadoxRootMetadata.Game {
    override val name: String get() = gameType.title
    override val version: String? = computeVersion()
    override val infoPath: Path? get() = null
    override val info: ParadoxRootMetadataInfo? get() = null
    override val infoPresentablePath: String? get() = null

    private fun computeVersion(): String? {
        return branchPath?.useLines { lines ->
            lines.firstOrNull()?.substringAfterLast('/')?.orNull()
        }
    }
}
