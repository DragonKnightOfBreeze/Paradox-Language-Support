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
    val source: ParadoxDefinitionSource,
    val name: String,
    val type: String,
    val fastSubtypes: List<String>,
    val typeKey: String,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val element: ParadoxDefinitionElement?
        get() = when (source) {
            ParadoxDefinitionSource.File -> file as? ParadoxScriptFile
            else -> file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) }
        }
    val fileElement: ParadoxScriptFile?
        get() = when (source) {
            ParadoxDefinitionSource.File -> file as? ParadoxScriptFile
            else -> null
        }
    val propertyElement: ParadoxScriptProperty?
        get() = when (source) {
            ParadoxDefinitionSource.File -> null
            else -> file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) }
        }
}
