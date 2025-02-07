package icu.windea.pls.ep.metadata

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxModDescriptorBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        //尝试在根目录中查找descriptor.mod
        val infoFile = runReadAction { ParadoxMetadataManager.getModDescriptorFile(rootFile) } ?: return null
        val info = ParadoxMetadataManager.getModDescriptorInfo(infoFile) ?: return null
        return Metadata(rootFile, infoFile, info)
    }

    class Metadata(
        rootFile: VirtualFile,
        val infoFile: VirtualFile,
        val info: ParadoxModDescriptorInfo
    ) : ParadoxMetadata.Mod {
        override val name: String get() = info.name
        override val version: String? get() = info.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType = doGetGameType()
        override val rootFile: VirtualFile = rootFile
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
                ?: getProfilesSettings().modDescriptorSettings.get(rootFile.path)?.gameType
                ?: getSettings().defaultGameType
        }
    }
}

/**
 * See: [Mod structure - Victoria 3 Wiki](https://vic3.paradoxwikis.com/index.php?title=Mod_structure)
 */
class ParadoxModMetadataBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        //尝试在根目录的.metadata子目录中中查找metadata.json
        val descriptorFile = runReadAction { ParadoxMetadataManager.getModMetadataFile(rootFile) } ?: return null
        val descriptorInfo = ParadoxMetadataManager.getModMetadataInfo(descriptorFile) ?: return null
        return Metadata(rootFile, descriptorFile, descriptorInfo)
    }

    class Metadata(
        rootFile: VirtualFile,
        val infoFile: VirtualFile,
        val info: ParadoxModMetadataInfo
    ) : ParadoxMetadata.Mod {
        override val name: String get() = info.name
        override val version: String? get() = info.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType = doGetGameType()
        override val rootFile: VirtualFile = rootFile
        override val entryFile: VirtualFile get() = rootFile

        override val supportedVersion: String? get() = info.supportedGameVersion
        override val picture: String? get() = info.picture?.orNull()?.let { ".metadata/$it" }
        override val tags: Set<String> get() = info.tags
        override val remoteId: String? get() = null
        override val source: ParadoxModSource get() = ParadoxModSource.Local

        private fun doGetInferredGameType(): ParadoxGameType? {
            return ParadoxCoreManager.getInferredGameType(rootFile)
        }

        private fun doGetGameType(): ParadoxGameType {
            return inferredGameType
                ?: doGetGameTypeFromInfo()
                ?: getProfilesSettings().modDescriptorSettings.get(rootFile.path)?.gameType
                ?: getSettings().defaultGameType
        }

        private fun doGetGameTypeFromInfo(): ParadoxGameType? {
            if (info.gameId == "victoria3") return ParadoxGameType.Vic3
            return null
        }
    }
}
