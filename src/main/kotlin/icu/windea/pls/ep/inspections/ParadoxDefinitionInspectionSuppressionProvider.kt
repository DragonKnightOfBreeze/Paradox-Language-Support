package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * @see icu.windea.pls.lang.inspections.ParadoxScriptInspectionSuppressor
 */
@WithGameTypeEP
interface ParadoxDefinitionInspectionSuppressionProvider {
    fun getSuppressedToolIds(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionInspectionSuppressionProvider>("icu.windea.pls.definitionInspectionSuppressionProvider")

        fun getSuppressedToolIds(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
            val gameType = definitionInfo.gameType
            val result = mutableSetOf<String>()
            EP_NAME.extensionList.forEach { ep ->
                if (!gameType.supportsByAnnotation(ep)) return@forEach
                result += ep.getSuppressedToolIds(definition, definitionInfo)
            }
            return result
        }
    }
}
