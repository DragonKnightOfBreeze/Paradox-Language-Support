package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxRootMetadata

/**
 * 提供游戏或模组的元数据。
 *
 * @see ParadoxRootMetadata
 */
interface ParadoxRootMetadataProvider {
    fun get(rootFile: VirtualFile): ParadoxRootMetadata?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxRootMetadataProvider>("icu.windea.pls.rootMetadataProvider")
    }
}
