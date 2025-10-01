package icu.windea.pls.ep.metadata

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxMetadata
import icu.windea.pls.model.ParadoxModDescriptorInfo
import icu.windea.pls.model.ParadoxModMetadataInfo
import icu.windea.pls.model.ParadoxModSource

class ParadoxModDescriptorBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        // 尝试在根目录中查找descriptor.mod

        val infoFile = runReadAction { ParadoxMetadataManager.getModDescriptorFile(rootFile) } ?: return null
        val info = ParadoxMetadataManager.getModDescriptorInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        override val rootFile: VirtualFile,
        val infoFile: VirtualFile,
        val info: ParadoxModDescriptorInfo
    ) : ParadoxMetadata.Mod {
        override val name: String get() = info.name
        override val version: String? get() = info.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType = doGetGameType()
        override val entryFile: VirtualFile get() = rootFile

        override val supportedVersion: String? get() = info.supportedVersion
        override val picture: String? get() = info.picture?.orNull()
        override val tags: Set<String> get() = info.tags
        override val remoteId: String? get() = info.remoteFileId
        override val source: ParadoxModSource get() = if (remoteId != null) ParadoxModSource.Steam else ParadoxModSource.Local

        private fun doGetInferredGameType(): ParadoxGameType? {
            return ParadoxCoreManager.getInferredGameType(rootFile)
        }

        private fun doGetGameType(): ParadoxGameType {
            return inferredGameType
                ?: PlsFacade.getProfilesSettings().modDescriptorSettings.get(rootFile.path)?.gameType
                ?: PlsFacade.getSettings().defaultGameType
        }
    }
}

/**
 * 参见：[Mod structure - Victoria 3 Wiki](https://vic3.paradoxwikis.com/index.php?title=Mod_structure)
 */
class ParadoxModMetadataBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        // 尝试在根目录的.metadata子目录中查找metadata.json

        val infoFile = runReadAction { ParadoxMetadataManager.getModMetadataFile(rootFile) } ?: return null
        val info = ParadoxMetadataManager.getModMetadataInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        override val rootFile: VirtualFile,
        val infoFile: VirtualFile,
        val info: ParadoxModMetadataInfo,
    ) : ParadoxMetadata.Mod {
        override val name: String get() = info.name
        override val version: String? get() = info.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType = doGetGameType()
        override val entryFile: VirtualFile get() = rootFile

        override val supportedVersion: String? get() = info.supportedGameVersion
        override val picture: String? get() = info.picture?.orNull()?.let { ".metadata/$it" }
        override val tags: Set<String> get() = info.tags
        override val remoteId: String? get() = null
        override val source: ParadoxModSource get() = ParadoxModSource.Local

        private fun doGetInferredGameType(): ParadoxGameType? {
            val gameTypeFromInfo = doGetInferredGameTypeFromInfo()
            if (gameTypeFromInfo != null) return gameTypeFromInfo
            return ParadoxCoreManager.getInferredGameType(rootFile)
        }

        private fun doGetInferredGameTypeFromInfo(): ParadoxGameType? {
            return when (info.gameId) {
                "victoria3" -> ParadoxGameType.Vic3
                else -> ParadoxGameType.Vic3 // #134 by default vic3
            }
        }

        private fun doGetGameType(): ParadoxGameType {
            return inferredGameType
                ?: PlsFacade.getProfilesSettings().modDescriptorSettings.get(rootFile.path)?.gameType
                ?: PlsFacade.getSettings().defaultGameType
        }
    }
}
