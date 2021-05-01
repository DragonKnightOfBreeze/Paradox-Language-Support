package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.model.*

class ParadoxRuleGroupsCache(
	ruleGroups:Map<String, ParadoxRuleGroup>
) {
	val paradoxRuleGroups = ruleGroups
	
	val coreParadoxRuleGroup = paradoxRuleGroups["core"]?:error("Core rule file 'rules/*.yml' is missed.")
	
	val paradoxLocales = coreParadoxRuleGroup.enums.getValue("locale").data.mapArray { ParadoxLocale(it.cast()) }
	val paradoxLocaleMap = paradoxLocales.associateBy { it.name }
	
	val paradoxSequentialNumbers = coreParadoxRuleGroup.enums.getValue("sequentialNumber").data.mapArray { ParadoxSequentialNumber(it.cast()) }
	val paradoxSequentialNumberMap = paradoxSequentialNumbers.associateBy { it.name }
	
	val paradoxColors = coreParadoxRuleGroup.enums.getValue("color").data.mapArray { ParadoxColor(it.cast()) }
	val paradoxColorMap = paradoxColors.associateBy { it.name }
	
	val paradoxCommandScopes = coreParadoxRuleGroup.enums.getValue("commandScope").data.mapArray { ParadoxCommandScope(it.cast()) }
	val paradoxPrimaryCommandScopes = paradoxCommandScopes.filter { it.isPrimary }.toTypedArray()
	val paradoxSecondaryCommandScopes = paradoxCommandScopes.filter{it.isSecondary}.toTypedArray()
	val paradoxCommandScopeMap = paradoxCommandScopes.associateBy { it.name }
	val paradoxPrimaryCommandScopeMap = paradoxCommandScopeMap.filterValues { it.isPrimary }
	val paradoxSecondaryCommandScopeMap = paradoxCommandScopeMap.filterValues { it.isSecondary }
	
	val paradoxCommandFields = coreParadoxRuleGroup.enums.getValue("commandField").data.mapArray { ParadoxCommandField(it.cast()) }
	val paradoxCommandFieldMap = paradoxCommandFields.associateBy { it.name }
}