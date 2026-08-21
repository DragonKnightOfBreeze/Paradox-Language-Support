package icu.windea.pls.model.index

import icu.windea.pls.ep.index.ParadoxScopeInferrableDefinitionMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService

/**
 * @see ParadoxMergedIndex
 * @see ParadoxScopeInferrableDefinitionMergedIndexSupport
 */
data class ParadoxScopeInferrableDefinitionIndexInfo(
    val definitionName: String,
    val typeExpression: String,
    val definitionElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val definitionElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxScriptPsiService.findPropertyFromStartOffset(file, definitionElementOffset) }
}
