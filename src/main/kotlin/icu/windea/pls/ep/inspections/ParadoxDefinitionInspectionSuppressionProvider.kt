package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * @see icu.windea.pls.lang.inspections.suppress.ParadoxScriptInspectionSuppressor
 */
@WithGameTypeEP
interface ParadoxDefinitionInspectionSuppressionProvider {
    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionInspectionSuppressionProvider>("icu.windea.pls.definitionInspectionSuppressionProvider")

    }
}
