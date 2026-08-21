package icu.windea.pls.lang.index.constraints

import com.intellij.util.indexing.ID
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtTypeModel
import icu.windea.pls.ep.resolve.localisation.ParadoxCompositeLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.ep.resolve.modifier.ParadoxEconomicCategoryModifierSupport
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.lang.index.ParadoxDefinitionConstrainedIndex
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextColorPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextFormatPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextIconPsiReference
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.model.orSpecific

/**
 * 定义索引的索引约束。
 *
 * @property indexId 对应的受约束索引使用的 [ID]。
 * @property ignoreCase 对应的受约束索引在索引定义的名字时，是否忽略大小写。
 *
 * @see ParadoxDefinitionConstrainedIndex
 */
enum class ParadoxDefinitionIndexConstraint(
    val indexId: ID<String, List<ParadoxDefinitionIndexInfo>>,
    val ignoreCase: Boolean = false,
    val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxDefinitionIndexInfo> {
    /**
     * @see ParadoxEconomicCategoryModifierSupport
     */
    EconomicCategory(ChronicleIndexKeys.DefinitionForEconomicCategory) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            val gameType = configGroup.gameType
            if (gameType.orSpecific() != null && gameType != ParadoxGameType.Stellaris) return false
            return definitionType == ParadoxDefinitionTypes.economicCategory
        }
    },
    /**
     * @see ParadoxLocalisationTextColorPsiReference
     */
    TextColor(ChronicleIndexKeys.DefinitionForTextColor) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            return definitionType == ParadoxDefinitionTypes.textColor
        }
    },
    /**
     * @see ParadoxLocalisationTextIconPsiReference
     */
    TextIcon(ChronicleIndexKeys.DefinitionForTextIcon) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            val gameType = configGroup.gameType
            if (!ParadoxSyntaxConstraint.LocalisationTextIcon.test(gameType)) return false
            return definitionType == ParadoxDefinitionTypes.textIcon
        }
    },
    /**
     * @see ParadoxLocalisationTextFormatPsiReference
     */
    TextFormat(ChronicleIndexKeys.DefinitionForTextFormat, ignoreCase = true) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            val gameType = configGroup.gameType
            if (!ParadoxSyntaxConstraint.LocalisationTextFormat.test(gameType)) return false
            return definitionType == ParadoxDefinitionTypes.textFormat
        }
    },
    /**
     * @see ParadoxLocalisationIconPsiReference
     * @see ParadoxLocalisationIconSupport
     * @see ParadoxCompositeLocalisationIconSupport.fromDefinition
     * @see CwtTypeModel.localisationIconResolvable
     */
    LocalisationIconResolvable(ChronicleIndexKeys.DefinitionForLocalisationIconResolvable) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            return configGroup.typeModel.localisationIconResolvable.contains(definitionType)
        }
    },
    ;

    abstract fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean
}
