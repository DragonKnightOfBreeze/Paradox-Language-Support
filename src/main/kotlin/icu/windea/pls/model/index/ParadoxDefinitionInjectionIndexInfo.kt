package icu.windea.pls.model.index

import icu.windea.pls.lang.index.ParadoxDefinitionInjectionIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService

/**
 * @see ParadoxDefinitionInjectionIndex
 */
data class ParadoxDefinitionInjectionIndexInfo(
    val mode: String,
    val target: String,
    val type: String,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val element: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxScriptPsiService.findPropertyFromStartOffset(file, elementOffset) }
}
