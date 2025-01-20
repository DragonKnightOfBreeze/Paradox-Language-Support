package icu.windea.pls.ep.metadata

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

/**
 * 用于获取游戏或模组的元数据。
 *
 * @see ParadoxMetadata
 */
@WithGameTypeEP
interface ParadoxMetadataProvider {
    fun getMetadata(rootFile: VirtualFile): ParadoxMetadata?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxMetadataProvider>("icu.windea.pls.metadataProvider")

        fun getMetadata(rootFile: VirtualFile): ParadoxMetadata? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                ep.getMetadata(rootFile)?.takeIf { it.gameType.supportsByAnnotation(ep) }
            }
        }
    }
}
