package icu.windea.pls.model.index

import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

data class ParadoxInferredScopeContextAwareDefinitionIndexInfo(
    val definitionName: String,
    val typeExpression: String,
    val definitionElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val definitionElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, definitionElementOffset) }
            ?.takeIf { ParadoxPsiMatcher.isDefinition(it) }
}
