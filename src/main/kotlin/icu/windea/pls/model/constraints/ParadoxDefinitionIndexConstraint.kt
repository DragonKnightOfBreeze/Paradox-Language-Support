package icu.windea.pls.model.constraints

import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo

enum class ParadoxDefinitionIndexConstraint(
    val definitionType: String,
    override val ignoreCase: Boolean = false,
    override val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxDefinitionIndexInfo> {
    TextColor(ParadoxDefinitionTypes.textColor),
    TextIcon(ParadoxDefinitionTypes.textIcon),
    TextFormat(ParadoxDefinitionTypes.textFormat, ignoreCase = true),
    ;

    open fun test(definitionType: String): Boolean = definitionType == this.definitionType

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.definitionType }

        @JvmStatic
        fun get(definitionType: String): ParadoxDefinitionIndexConstraint? = map[definitionType]
    }
}
