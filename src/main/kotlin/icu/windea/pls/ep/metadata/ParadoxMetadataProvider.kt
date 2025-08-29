package icu.windea.pls.ep.metadata

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxMetadata

/**
 * 用于获取游戏或模组的元数据。
 *
 * @see ParadoxMetadata
 */
interface ParadoxMetadataProvider {
    fun getMetadata(rootFile: VirtualFile): ParadoxMetadata?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxMetadataProvider>("icu.windea.pls.metadataProvider")

        fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ep.getMetadata(rootFile)
            }
        }
    }
}
