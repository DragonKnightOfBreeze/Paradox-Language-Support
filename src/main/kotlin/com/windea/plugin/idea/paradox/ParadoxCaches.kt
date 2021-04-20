package com.windea.plugin.idea.paradox

import com.windea.plugin.idea.paradox.model.*
import com.windea.plugin.idea.paradox.util.*

//Caches 

val paradoxRuleGroups = ParadoxRuleGroupProvider.getRuleGroups()

private val coreParadoxRuleGroup = paradoxRuleGroups["core"]?:error("Core rule file 'rules/*.yml' is missed.")

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

val inferredParadoxLocale = when(System.getProperty("user.language")) {
	"zh" -> paradoxLocaleMap.getValue("l_simp_chinese")
	"en" -> paradoxLocaleMap.getValue("l_english")
	"pt" -> paradoxLocaleMap.getValue("l_braz_por")
	"fr" -> paradoxLocaleMap.getValue("l_french")
	"de" -> paradoxLocaleMap.getValue("l_german")
	"pl" -> paradoxLocaleMap.getValue("l_ponish")
	"ru" -> paradoxLocaleMap.getValue("l_russian")
	"es" -> paradoxLocaleMap.getValue("l_spanish")
	else -> paradoxLocaleMap.getValue("l_english")
}
