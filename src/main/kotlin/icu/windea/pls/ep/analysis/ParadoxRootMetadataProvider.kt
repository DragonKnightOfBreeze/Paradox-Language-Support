package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import java.nio.file.Path

/**
 * 提供游戏或模组的元数据。
 *
 * @see ParadoxRootMetadata
 */
interface ParadoxRootMetadataProvider {
    fun get(rootPath: Path): ParadoxRootMetadata?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxRootMetadataProvider>("icu.windea.pls.rootMetadataProvider")
    }
}
