package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.model.ParadoxGameType

/**
 * @property definitionElementOffset 所属的定义的 PSI 偏移。仅对应的 [CwtComplexEnumConfig] 附有 `## per_definition` 时才有值。
 */
data class ParadoxComplexEnumValueIndexInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val definitionElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null

    val config: CwtComplexEnumConfig?
        get() = PlsFacade.getConfigGroup(gameType).complexEnums[enumName]
    val caseInsensitive: Boolean
        get() = config?.caseInsensitive ?: false
}
