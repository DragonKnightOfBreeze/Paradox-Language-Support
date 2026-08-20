package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.inspections.suppress.ParadoxScriptInspectionSuppressor
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于在定义级别提供相关代码检查的抑制策略。
 *
 * @see ParadoxScriptInspectionSuppressor
 */
interface ParadoxDefinitionInspectionSuppressionProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxDefinitionInspectionSuppressionProvider>("icu.windea.pls.definitionInspectionSuppressionProvider")
        @JvmField val CACHE = LazyValue<List<ParadoxDefinitionInspectionSuppressionProvider>>()

        fun getAll(): List<ParadoxDefinitionInspectionSuppressionProvider> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.initialize { computeCache() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { computeCache() } }
        }

        private fun computeCache(): List<ParadoxDefinitionInspectionSuppressionProvider> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
