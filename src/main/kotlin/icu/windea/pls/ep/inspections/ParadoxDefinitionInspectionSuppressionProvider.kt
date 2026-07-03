package icu.windea.pls.ep.inspections

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.lang.inspections.suppress.ParadoxScriptInspectionSuppressor
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于在定义级别提供相关代码检查的抑制策略。
 *
 * @see ParadoxScriptInspectionSuppressor
 */
@WithGameTypeEP
interface ParadoxDefinitionInspectionSuppressionProvider {
    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxDefinitionInspectionSuppressionProvider>("icu.windea.pls.definitionInspectionSuppressionProvider")
    }
}
