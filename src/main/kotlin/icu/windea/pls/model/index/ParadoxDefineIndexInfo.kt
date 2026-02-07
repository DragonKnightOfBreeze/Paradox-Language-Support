package icu.windea.pls.model.index

import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

data class ParadoxDefineIndexInfo(
    val namespace: String,
    val variable: String?,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val element: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, elementOffset) }
            ?.takeIf { ParadoxDefineManager.isDefineElement(it, namespace, variable) }
}
