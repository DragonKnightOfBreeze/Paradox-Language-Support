package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import java.nio.file.Path

/**
 * 提供游戏或模组的元数据。
 *
 * @see ParadoxRootMetadata
 */
interface ParadoxRootMetadataProvider {
    fun getRootMetadata(rootPath: Path): ParadoxRootMetadata?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxRootMetadataProvider>("icu.windea.pls.rootMetadataProvider")
        @JvmField val CACHE = LazyValue<List<ParadoxRootMetadataProvider>>()

        fun getAll(): List<ParadoxRootMetadataProvider> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<ParadoxRootMetadataProvider> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
