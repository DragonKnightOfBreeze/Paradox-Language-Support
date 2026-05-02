package icu.windea.pls.model.analysis

import icu.windea.pls.core.orNull
import icu.windea.pls.lang.analysis.ParadoxAnalysisService
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Path

data class ParadoxDescriptorModBasedModMetadata(
    override val rootPath: Path,
    override val infoPath: Path,
    override val info: ParadoxDescriptorModInfo,
) : ParadoxRootMetadata.Mod {
    override val name: String get() = info.name
    override val version: String? get() = info.version
    override val inferredGameType: ParadoxGameType? = computeInferredGameType()
    override val gameType: ParadoxGameType = computeGameType()
    override val infoPresentablePath: String = "descriptor.mod"

    override val supportedVersion: String? get() = info.supportedVersion
    override val picture: String? get() = info.picture?.orNull()
    override val tags: Set<String> get() = info.tags
    override val remoteId: String? get() = info.remoteFileId
    override val source: ParadoxModSource get() = if (remoteId != null) ParadoxModSource.Steam else ParadoxModSource.Local

    private fun computeInferredGameType(): ParadoxGameType? {
        return ParadoxAnalysisService.getInferredGameType(rootPath)
    }

    private fun computeGameType(): ParadoxGameType {
        return inferredGameType
            ?: PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(rootPath.toString())?.gameType
            ?: ParadoxGameType.getDefault()
    }
}

data class ParadoxMetadataJsonBasedModMetadata(
    override val rootPath: Path,
    override val infoPath: Path,
    override val info: ParadoxMetadataJsonInfo,
) : ParadoxRootMetadata.Mod {
    override val name: String get() = info.name
    override val version: String? get() = info.version
    override val inferredGameType: ParadoxGameType? = computeInferredGameType()
    override val gameType: ParadoxGameType = computeGameType()
    override val infoPresentablePath: String = ".metadata/metadata.json"

    override val supportedVersion: String? get() = info.supportedGameVersion
    override val picture: String? get() = info.picture?.orNull()?.let { ".metadata/$it" }
    override val tags: Set<String> get() = info.tags
    override val remoteId: String? get() = null
    override val source: ParadoxModSource get() = ParadoxModSource.Local

    private fun computeInferredGameType(): ParadoxGameType? {
        return when (info.gameId) {
            ParadoxGameType.Vic3.gameId -> ParadoxGameType.Vic3
            ParadoxGameType.Eu5.gameId -> ParadoxGameType.Eu5
            else -> ParadoxAnalysisService.getInferredGameType(rootPath)
        }
    }

    private fun computeGameType(): ParadoxGameType {
        return inferredGameType
            ?: PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(rootPath.toString())?.gameType
            ?: ParadoxGameType.getDefault()
    }
}
