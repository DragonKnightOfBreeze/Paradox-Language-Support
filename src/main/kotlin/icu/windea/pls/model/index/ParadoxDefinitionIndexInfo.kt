package icu.windea.pls.model.index

import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * @see icu.windea.pls.lang.index.ParadoxDefinitionIndex
 */
data class ParadoxDefinitionIndexInfo(
    val name: String,
    val type: String,
    val subtypes: List<String>?,
    val typeKey: String,
    val source: ParadoxDefinitionSource,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val element: ParadoxDefinitionElement?
        get() = when (source) {
            ParadoxDefinitionSource.File -> fileElement
            else -> propertyElement
        }
    val fileElement: ParadoxScriptFile?
        get() = file as? ParadoxScriptFile
    val propertyElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) }
}
