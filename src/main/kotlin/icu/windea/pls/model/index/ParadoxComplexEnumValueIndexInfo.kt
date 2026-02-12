package icu.windea.pls.model.index

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * @property definitionElementOffset 所属的定义的 PSI 偏移。仅对应的 [CwtComplexEnumConfig] 附有 `## per_definition` 时才有值。
 *
 * @see icu.windea.pls.lang.index.ParadoxComplexEnumValueIndex
 */
data class ParadoxComplexEnumValueIndexInfo(
    val name: String,
    val enumName: String,
    val definitionElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val config: CwtComplexEnumConfig?
        get() = PlsFacade.getConfigGroup(gameType).complexEnums[enumName]
    val caseInsensitive: Boolean
        get() = config?.caseInsensitive ?: false
    val id: String
        get() = if (caseInsensitive) name.lowercase() else name // # 261

    val definitionElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, definitionElementOffset) }
            ?.takeIf { ParadoxPsiMatcher.isDefinition(it) || ParadoxPsiMatcher.isDefinitionInjection(it) }
}
