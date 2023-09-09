package icu.windea.pls.lang.data.impl

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEconomicCategoryDataProvider: ParadoxDefinitionDataProvider<StellarisEconomicCategoryDataProvider.Data>() {
    class Data : ParadoxDefinitionData() {
        val parent : String? by data.get("parent")
        val useForAiBudget : Boolean by data.get("use_for_ai_budget", false)
        val modifierCategory: String? by data.get("modifier_category")
        val generateAddModifiers: Set<String> by data.get("generate_add_modifiers", emptySet())
        val generateMultModifiers: Set<String> by data.get("generate_mult_modifiers", emptySet())
        val triggeredProducesModifiers: List<TriggeredModifier> by data.getAll("triggered_produces_modifier")
        val triggeredCostModifiers: List<TriggeredModifier> by data.getAll("triggered_cost_modifier")
        val triggeredUpkeepModifiers: List<TriggeredModifier> by data.getAll("triggered_upkeep_modifier")
        
        class TriggeredModifier(data: ParadoxScriptData): ParadoxDefinitionData() {
            val key: String by data.get("key", "")
            val useParentIcon: Boolean by data.get("use_parent_icon", false)
            val modifierTypes: Set<String> by data.get("modifier_types", emptySet())
        }
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "economic_category"
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisGameConceptDataProvider : ParadoxDefinitionDataProvider<StellarisGameConceptDataProvider.Data>() {
    class Data : ParadoxDefinitionData() {
        val icon: String? by data.get("icon")
        val alias: Set<String>? by data.get("alias")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "game_concept"
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyDataProvider : ParadoxDefinitionDataProvider<StellarisTechnologyDataProvider.Data>() {
    class Data : ParadoxDefinitionData() {
        val icon: String? by data.get("icon")
        val tier: String? by data.get("tier")
        val area: String? by data.get("area")
        val category: Set<String>? by data.get("category")
        
        val cost: Int? by data.get("cost")
        val cost_per_level: Int? by data.get("cost_per_level")
        val levels: Int? by data.get("levels")
        
        val start_tech: Boolean by data.get("start_tech", false)
        val is_rare: Boolean by data.get("is_rare", false)
        val is_dangerous: Boolean by data.get("is_dangerous", false)
        val is_insight: Boolean by data.get("is_insight", false)
        
        val gateway: String? by data.get("gateway")
        val prerequisites: Set<String> by data.get("prerequisites", emptySet())
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "technology"
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventDataProvider: ParadoxDefinitionDataProvider<StellarisEventDataProvider.Data>() {
    class Data : ParadoxDefinitionData() {
        val base: String? by data.get("base")
        val desc_clear: Boolean by data.get("desc_clear", false)
        val option_clear: Boolean by data.get("option_clear", false)
        val picture_clear: Boolean by data.get("picture_clear", false)
        val show_sound_clear: Boolean by data.get("show_sound_clear", false)
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "event"
    }
}