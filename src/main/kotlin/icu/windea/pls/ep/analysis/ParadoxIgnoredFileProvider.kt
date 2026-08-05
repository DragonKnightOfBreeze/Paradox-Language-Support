package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.paths.ParadoxPath

/**
 * 提供需要忽略的文件。
 *
 * 解析文件信息时，被忽略的文件不会被视为脚本文件、本地化文件或者 CSV 文件。其文件分组会被直接解析为 [ParadoxFileGroup.Other]。
 *
 * @see ParadoxFileGroup
 * @see ParadoxFileInfo
 */
interface ParadoxIgnoredFileProvider {
    fun isIgnoredFile(path: ParadoxPath, entry: String): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxIgnoredFileProvider>("icu.windea.pls.ignoredFileProvider")
        @JvmField val CACHE = LazyValue<List<ParadoxIgnoredFileProvider>>()

        fun getAll(): List<ParadoxIgnoredFileProvider> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<ParadoxIgnoredFileProvider> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
