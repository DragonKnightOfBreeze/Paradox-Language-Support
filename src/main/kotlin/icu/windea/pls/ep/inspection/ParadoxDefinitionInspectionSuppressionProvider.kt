package icu.windea.pls.ep.inspection

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.lang.inspections.ParadoxScriptInspectionSuppressor
 */
@WithGameTypeEP
interface ParadoxDefinitionInspectionSuppressionProvider {
    fun getSuppressedToolIds(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.Companion.create<ParadoxDefinitionInspectionSuppressionProvider>("icu.windea.pls.definitionInspectionSuppressionProvider")

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
