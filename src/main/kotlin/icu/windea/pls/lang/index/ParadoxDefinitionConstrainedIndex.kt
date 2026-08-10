package icu.windea.pls.lang.index

import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.asMutable
import icu.windea.pls.core.letIf
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.index.statistics.ChronicleIndexStatisticService
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextColorPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextFormatPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextIconPsiReference
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo

/**
 * 定义信息的受约束索引。
 *
 * 用于优化和调整符合特定约束的定义声明的索引逻辑。
 *
 * @see ParadoxDefinitionIndex
 * @see ParadoxDefinitionIndexConstraint
 */
abstract class ParadoxDefinitionConstrainedIndex : ParadoxDefinitionIndex() {
    abstract val constraint: ParadoxDefinitionIndexConstraint

    override fun getName() = constraint.indexId

    override fun getFileLevelTypeConfigs(matchContext: CwtTypeConfigMatchContext): Collection<CwtTypeConfig> {
        val result = super.getFileLevelTypeConfigs(matchContext).filter { constraint.test(it.name, it.configGroup) }
        if (result.isEmpty()) return emptyList()
        return result
    }

    override fun addToFileData(info: ParadoxDefinitionIndexInfo, fileData: MutableMap<String, List<ParadoxDefinitionIndexInfo>>, configGroup: CwtConfigGroup) {
        ChronicleIndexStatisticService.recordDefinitionConstrained(info.gameType, constraint)

        val ignoreCase = constraint.ignoreCase
        val name = info.name.letIf(ignoreCase) { it.lowercase() }
        val type = info.type
        fileData.getOrPut(ChronicleIndexUtil.createAllKey()) { mutableListOf() }.asMutable() += info
        fileData.getOrPut(ChronicleIndexUtil.createTypeKey(type)) { mutableListOf() }.asMutable() += info
        if (name.isEmpty()) return
        fileData.getOrPut(ChronicleIndexUtil.createNameKey(name)) { mutableListOf() }.asMutable() += info
        fileData.getOrPut(ChronicleIndexUtil.createNameTypeKey(name, type)) { mutableListOf() }.asMutable() += info
    }

    /**
     * 用于快速索引经济分类。对于 Stellaris，会额外从经济分类生成修正。
     *
     * @see ParadoxDefinitionIndexConstraint.EconomicCategory
     */
    class EconomicCategoryIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.EconomicCategory
    }

    /**
     * 用于快速索引文本颜色。它们是 [ParadoxLocalisationTextColorPsiReference] 的解析目标。
     *
     * @see ParadoxDefinitionIndexConstraint.TextColor
     */
    class TextColorIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.TextColor
    }

    /**
     * 用于快速索引文本图标。它们是 [ParadoxLocalisationTextIconPsiReference] 的解析目标。
     *
     * @see ParadoxDefinitionIndexConstraint.TextIcon
     */
    class TextIconIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.TextIcon
    }

    /**
     * 用于快速索引文本格式。它们是 [ParadoxLocalisationTextFormatPsiReference] 的解析目标。
     *
     * @see ParadoxDefinitionIndexConstraint.TextFormat
     */
    class TextFormatIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.TextFormat
    }

    /**
     * 用于快速索引可能是 [ParadoxLocalisationIconPsiReference] 的解析目标的定义信息。
     *
     * @see ParadoxDefinitionIndexConstraint.LocalisationIconResolvable
     */
    class LocalisationIconResolvableIndex : ParadoxDefinitionConstrainedIndex() {
        override val constraint get() = ParadoxDefinitionIndexConstraint.LocalisationIconResolvable
    }
}
