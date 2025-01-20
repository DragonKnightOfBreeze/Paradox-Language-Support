package icu.windea.pls.ep.metadata

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

class ParadoxModDescriptorBasedMetadataProvider : ParadoxMetadataProvider {
    override fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
        //尝试从根目录向下查找descriptor.mod
        val descriptorFile = runReadAction { ParadoxMetadataManager.getDescriptorFile(rootFile) } ?: return null
        val descriptorInfo = ParadoxMetadataManager.getDescriptorInfo(descriptorFile) ?: return null
        return Metadata(rootFile, descriptorFile, descriptorInfo)
    }

    class Metadata(
        rootFile: VirtualFile,
        val descriptorFile: VirtualFile,
        val descriptorInfo: ParadoxModDescriptorInfo
    ) : ParadoxMetadata {
        override val forGame: Boolean get() = false
        override val name: String get() = descriptorInfo.name
        override val version: String? get() = descriptorInfo.version
        override val inferredGameType: ParadoxGameType? = doGetInferredGameType()
        override val gameType: ParadoxGameType  = doGetGameType()
        override val rootFile: VirtualFile = rootFile
        override val entryFile: VirtualFile get() = rootFile

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
