package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.model.*

class ParadoxRuleGroupsCache(
	val ruleGroups: Map<String, ParadoxRuleGroup>
) {
	//val coreRuleGroup = this.ruleGroups["core"]?:error("Core rule file 'rules/*.yml' is missed.")
	//
	//val locales = coreRuleGroup.enums.getValue("locale").data.mapArray { ParadoxLocale(it.cast()) }
	//val localeMap = locales.associateBy { it.name }
	//
	//val sequentialNumbers = coreRuleGroup.enums.getValue("sequentialNumber").data.mapArray { ParadoxSequentialNumber(it.cast()) }
	//val sequentialNumberMap = sequentialNumbers.associateBy { it.name }
	//
	//val colors = coreRuleGroup.enums.getValue("color").data.mapArray { ParadoxColor(it.cast()) }
	//val colorMap = colors.associateBy { it.name }
	
	//val commandScopes = coreRuleGroup.enums.getValue("commandScope").data.mapArray { ParadoxCommandScope(it.cast()) }
	//val primaryCommandScopes = commandScopes.filter { it.isPrimary }.toTypedArray()
	//val secondaryCommandScopes = commandScopes.filter{it.isSecondary}.toTypedArray()
	//val commandScopeMap = commandScopes.associateBy { it.name }
	//val primaryCommandScopeMap = commandScopeMap.filterValues { it.isPrimary }
	//val secondaryCommandScopeMap = commandScopeMap.filterValues { it.isSecondary }
	//
	//val commandFields = coreRuleGroup.enums.getValue("commandField").data.mapArray { ParadoxCommandField(it.cast()) }
	//val commandFieldMap = commandFields.associateBy { it.name }
}