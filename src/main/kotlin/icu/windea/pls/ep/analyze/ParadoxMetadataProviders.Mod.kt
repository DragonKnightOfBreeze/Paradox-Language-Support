package icu.windea.pls.ep.analyze

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.analyze.ParadoxAnalyzeService
import icu.windea.pls.lang.analyze.ParadoxMetadata
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.metadata.ParadoxDescriptorModInfo
import icu.windea.pls.model.metadata.ParadoxMetadataJsonInfo

class ParadoxDescriptorModBasedModMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        // 尝试在根目录中查找 `descriptor.mod`

        val infoFile = ParadoxMetadataManager.getDescriptorModFile(rootFile) ?: return null
        val info = ParadoxMetadataManager.getDescriptorModInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        override val rootFile: VirtualFile,
        override val infoFile: VirtualFile,
        val info: ParadoxDescriptorModInfo
    ) : ParadoxMetadata.Mod {
        override val name: String get() = info.name
        override val version: String? get() = info.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType = doGetGameType()

        override val supportedVersion: String? get() = info.supportedVersion
        override val picture: String? get() = info.picture?.orNull()
        override val tags: Set<String> get() = info.tags
        override val remoteId: String? get() = info.remoteFileId
        override val source: ParadoxModSource get() = if (remoteId != null) ParadoxModSource.Steam else ParadoxModSource.Local

        private fun doGetInferredGameType(): ParadoxGameType? {
            return ParadoxAnalyzeService.getInferredGameType(rootFile)
        }

        private fun doGetGameType(): ParadoxGameType {
            return inferredGameType
                ?: PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(rootFile.path)?.gameType
                ?: ParadoxGameType.getDefault()
        }
    }
}

/**
 * 参见：[Mod structure - Victoria 3 Wiki](https://vic3.paradoxwikis.com/index.php?title=Mod_structure)
 */
class ParadoxMetadataJsonBasedModMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        // 尝试在根目录的 `.metadata` 子目录中查找 `metadata.json`

        val infoFile = ParadoxMetadataManager.getMetadataJsonFile(rootFile) ?: return null
        val info = ParadoxMetadataManager.getMetadataJsonInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        override val rootFile: VirtualFile,
        override val infoFile: VirtualFile,
        val info: ParadoxMetadataJsonInfo,
    ) : ParadoxMetadata.Mod {
        override val name: String get() = info.name
        override val version: String? get() = info.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType = doGetGameType()

        override val supportedVersion: String? get() = info.supportedGameVersion
        override val picture: String? get() = info.picture?.orNull()?.let { ".metadata/$it" }
        override val tags: Set<String> get() = info.tags
        override val remoteId: String? get() = null
        override val source: ParadoxModSource get() = ParadoxModSource.Local

        private fun doGetInferredGameType(): ParadoxGameType? {
            return when (info.gameId) {
                ParadoxGameType.Vic3.gameId -> ParadoxGameType.Vic3
                ParadoxGameType.Eu5.gameId -> ParadoxGameType.Eu5
                else -> ParadoxAnalyzeService.getInferredGameType(rootFile)
            }
        }

        private fun doGetGameType(): ParadoxGameType {
            return inferredGameType
                ?: PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(rootFile.path)?.gameType
                ?: ParadoxGameType.getDefault()
        }
    }
}
