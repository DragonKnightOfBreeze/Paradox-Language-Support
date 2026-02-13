package icu.windea.pls.model.index

import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * @see icu.windea.pls.lang.index.ParadoxDefinitionIndex
 */
data class ParadoxDefinitionIndexInfo(
    val source: ParadoxDefinitionSource,
    val name: String,
    val type: String,
    val subtypes: List<String>?,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val element: ParadoxDefinitionElement?
        get() = when (source) {
            ParadoxDefinitionSource.File -> file as? ParadoxDefinitionElement
            else -> file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) as? ParadoxDefinitionElement }
        }

    val propertyElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) }
}
