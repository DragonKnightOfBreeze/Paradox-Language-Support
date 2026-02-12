package icu.windea.pls.model.index

import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

/**
 * @see icu.windea.pls.lang.index.ParadoxDefinitionInjectionIndex
 */
data class ParadoxDefinitionInjectionIndexInfo(
    val mode: String,
    val target: String,
    val type: String,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val element: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) }
            ?.takeIf { it.parent is ParadoxScriptRootBlock }
            ?.takeIf { it.propertyValue is ParadoxScriptBlock }
            ?.takeIf { ParadoxDefinitionInjectionManager.getModeFromExpression(it.name)?.equals(mode, true) == true }
            ?.takeIf { ParadoxDefinitionInjectionManager.getTargetFromExpression(it.name) == target }
}
