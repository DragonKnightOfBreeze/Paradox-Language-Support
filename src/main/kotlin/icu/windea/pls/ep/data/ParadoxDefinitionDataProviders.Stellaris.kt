@file:Suppress("unused")

package icu.windea.pls.ep.data

import icu.windea.pls.lang.annotations.WithDefinitionType
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.util.data.ParadoxScriptData
import icu.windea.pls.lang.util.data.get
import icu.windea.pls.lang.util.data.getAll
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

@WithGameType(ParadoxGameType.Stellaris)
@WithDefinitionType(ParadoxDefinitionTypes.EconomicCategory)
class StellarisEconomicCategoryData(data: ParadoxScriptData) : ParadoxDefinitionDataBase(data) {
    val parent: String? by data.get("parent")
    val useForAiBudget: Boolean by data.get("use_for_ai_budget", false)
    val modifierCategory: String? by data.get("modifier_category")
    val generateAddModifiers: Set<String> by data.get("generate_add_modifiers", emptySet())
    val generateMultModifiers: Set<String> by data.get("generate_mult_modifiers", emptySet())
    val triggeredProducesModifiers: List<TriggeredModifier> by data.getAll("triggered_produces_modifier")
    val triggeredCostModifiers: List<TriggeredModifier> by data.getAll("triggered_cost_modifier")
    val triggeredUpkeepModifiers: List<TriggeredModifier> by data.getAll("triggered_upkeep_modifier")

    class TriggeredModifier(data: ParadoxScriptData) : ParadoxDefinitionData {
        val key: String by data.get("key", "")
        val useParentIcon: Boolean by data.get("use_parent_icon", false)
        val modifierTypes: Set<String> by data.get("modifier_types", emptySet())
    }
}

@WithGameType(ParadoxGameType.Stellaris)
@WithDefinitionType(ParadoxDefinitionTypes.GameConcept)
class StellarisGameConceptData(data: ParadoxScriptData) : ParadoxDefinitionDataBase(data) {
    val icon: String? by data.get("icon")
    val tooltipOverride: String? by data.get("tooltip_override")
    val alias: Set<String>? by data.get("alias")
}

@WithGameType(ParadoxGameType.Stellaris)
@WithDefinitionType(ParadoxDefinitionTypes.Technology)
class StellarisTechnologyData(data: ParadoxScriptData) : ParadoxDefinitionDataBase(data) {
    val icon: String? by data.get("icon")
    val tier: String? by data.get("tier")
    val area: String? by data.get("area")
    val category: Set<String>? by data.get("category")

    val cost: Int? by data.get("cost")
    val costPerLevel: Int? by data.get("cost_per_level")
    val levels: Int? by data.get("levels")

    val startTech: Boolean by data.get("start_tech", false)
    val isRare: Boolean by data.get("is_rare", false)
    val isDangerous: Boolean by data.get("is_dangerous", false)
    val isInsight: Boolean by data.get("is_insight", false)

    val gateway: String? by data.get("gateway")
    val prerequisites: Set<String> by data.get("prerequisites", emptySet())
}

@WithGameType(ParadoxGameType.Stellaris)
@WithDefinitionType(ParadoxDefinitionTypes.Event)
class StellarisEventData(data: ParadoxScriptData) : ParadoxDefinitionDataBase(data) {
    val base: String? by data.get("base")
    val descClear: Boolean by data.get("desc_clear", false)
    val optionClear: Boolean by data.get("option_clear", false)
    val pictureClear: Boolean by data.get("picture_clear", false)
    val showSoundClear: Boolean by data.get("show_sound_clear", false)
}
