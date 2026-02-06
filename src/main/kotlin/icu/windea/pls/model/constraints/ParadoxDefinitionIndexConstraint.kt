package icu.windea.pls.model.constraints

import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxDefinitionElement

enum class ParadoxDefinitionIndexConstraint(
    override val indexKey: StubIndexKey<String, ParadoxDefinitionElement>,
    val definitionType: String,
    override val ignoreCase: Boolean = false,
    override val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxDefinitionElement> {
    Resource(PlsIndexKeys.DefinitionNameForResource, ParadoxDefinitionTypes.resource),
    EconomicCategory(PlsIndexKeys.DefinitionNameForEconomicCategory, ParadoxDefinitionTypes.economicCategory),
    GameConcept(PlsIndexKeys.DefinitionNameForGameConcept, ParadoxDefinitionTypes.gameConcept),
    EventNamespace(PlsIndexKeys.DefinitionNameForEventNamespace, ParadoxDefinitionTypes.eventNamespace),
    Event(PlsIndexKeys.DefinitionNameForEvent, ParadoxDefinitionTypes.event),
    Sprite(PlsIndexKeys.DefinitionNameForSprite, ParadoxDefinitionTypes.sprite),
    TextColor(PlsIndexKeys.DefinitionNameForTextColor, ParadoxDefinitionTypes.textColor),
    TextIcon(PlsIndexKeys.DefinitionNameForTextIcon, ParadoxDefinitionTypes.textIcon),
    TextFormat(PlsIndexKeys.DefinitionNameForTextFormat, ParadoxDefinitionTypes.textFormat, ignoreCase = true),
    ;

    open fun test(definitionType: String): Boolean = definitionType == this.definitionType

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.definitionType }

        @JvmStatic
        fun get(definitionType: String): ParadoxDefinitionIndexConstraint? = map[definitionType]
    }
}
