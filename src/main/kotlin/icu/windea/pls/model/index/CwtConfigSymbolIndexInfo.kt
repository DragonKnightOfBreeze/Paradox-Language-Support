package icu.windea.pls.model.index

import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.cwt.psi.CwtPsiService
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.lang.index.CwtConfigIndexInfoAwareFileBasedIndex
import icu.windea.pls.lang.psi.light.CwtConfigSymbolLightElement
import icu.windea.pls.model.ParadoxGameType

/**
 * @see CwtConfigSymbolLightElement
 * @see CwtConfigIndexInfoAwareFileBasedIndex
 */
data class CwtConfigSymbolIndexInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccess,
    val offset: Int,
    val elementOffset: Int,
    override val gameType: ParadoxGameType
) : CwtConfigIndexInfo() {
    val element: CwtStringExpressionElement?
        get() = file?.let { file -> CwtPsiService.findStringExpressionElementFromStartOffset(file, elementOffset) }
}
