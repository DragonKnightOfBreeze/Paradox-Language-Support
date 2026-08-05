package icu.windea.pls.ep.analysis

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameTypeInfo
import java.nio.file.Path

/**
 * 提供推断的游戏类型。
 *
 * @see ParadoxGameType
 * @see ParadoxGameTypeInfo
 */
interface ParadoxInferredGameTypeProvider {
    fun getInferredGameTypeInfo(rootPath: Path): ParadoxGameTypeInfo?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxInferredGameTypeProvider>("icu.windea.pls.inferredGameTypeProvider")
        @JvmField val CACHE = LazyValue<List<ParadoxInferredGameTypeProvider>>()

        fun getAll(): List<ParadoxInferredGameTypeProvider> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<ParadoxInferredGameTypeProvider> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
