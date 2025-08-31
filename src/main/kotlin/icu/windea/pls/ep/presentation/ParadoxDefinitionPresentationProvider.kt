package icu.windea.pls.ep.presentation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于绘制定义的图形表示。
 */
@WithGameTypeEP
interface ParadoxDefinitionPresentationProvider<T : ParadoxDefinitionPresentationData> {
    val type: Class<T>

    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getPresentationData(definition: ParadoxScriptDefinitionElement): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionPresentationProvider<*>>("icu.windea.pls.definitionPresentationProvider")

        @Suppress("UNCHECKED_CAST")
        fun <T : ParadoxDefinitionPresentationData> getPresentationData(type: Class<T>, definition: ParadoxScriptDefinitionElement): T? {
            val definitionInfo = definition.definitionInfo ?: return null
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                if (ep.type != type) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getPresentationData(definition) as? T?
            }
        }
    }
}
