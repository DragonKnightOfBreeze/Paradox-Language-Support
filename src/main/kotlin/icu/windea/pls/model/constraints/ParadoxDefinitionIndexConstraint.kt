package icu.windea.pls.model.constraints

import com.intellij.util.indexing.ID
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtTypesModel
import icu.windea.pls.ep.resolve.localisation.ParadoxCompositeLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo

enum class ParadoxDefinitionIndexConstraint(
    val indexKey: ID<String, List<ParadoxDefinitionIndexInfo>>,
    val ignoreCase: Boolean = false,
    val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxDefinitionIndexInfo> {
    TextColor(ChronicleIndexKeys.DefinitionForTextColor) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            return definitionType == ParadoxDefinitionTypes.textColor
        }
    },
    TextIcon(ChronicleIndexKeys.DefinitionForTextIcon) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            if (!ParadoxSyntaxConstraint.LocalisationTextIcon.test(configGroup.gameType)) return false
            return definitionType == ParadoxDefinitionTypes.textIcon
        }
    },
    TextFormat(ChronicleIndexKeys.DefinitionForTextFormat, ignoreCase = true) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            if (!ParadoxSyntaxConstraint.LocalisationTextFormat.test(configGroup.gameType)) return false
            return definitionType == ParadoxDefinitionTypes.textFormat
        }
    },
    /**
     * @see ParadoxLocalisationIconSupport
     * @see ParadoxCompositeLocalisationIconSupport.fromDefinition
     * @see CwtTypesModel.localisationIconResolvable
     */
    LocalisationIconResolvable(ChronicleIndexKeys.DefinitionForLocalisationIconResolvable) {
        override fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean {
            return configGroup.typesModel.localisationIconResolvable.contains(definitionType)
        }
    },
    ;

    abstract fun test(definitionType: String, configGroup: CwtConfigGroup): Boolean
}
