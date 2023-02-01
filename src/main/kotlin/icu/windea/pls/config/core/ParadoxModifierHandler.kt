package icu.windea.pls.config.core

import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
	//可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
	//修正会由特定的定义类型生成
	//TODO 修正会由经济类型（economic_category）的声明生成
	
	@JvmStatic
	fun matchesModifier(name: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		val modifierName = name.lowercase()
		//预定义的非生成的修正
		val predefinedModifierConfig = configGroup.predefinedModifiers[modifierName]
		if(predefinedModifierConfig != null) return true
		val isStatic = BitUtil.isSet(matchType, CwtConfigMatchType.STATIC)
		if(isStatic) return false
		//生成的修正，生成源必须已定义
		return configGroup.generatedModifiers.values.any { config ->
			config.template.matches(name, configGroup, matchType)
		}
	}
	
	@JvmStatic
	fun resolveModifier(element: ParadoxScriptStringExpressionElement) : ParadoxModifierElement? {
		val name = element.value
		val gameType = selectGameType(element) ?: return null
		val project = element.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return resolveModifier(element, name, configGroup)
	}
	
	@JvmStatic
	fun resolveModifier(element: ParadoxScriptStringExpressionElement, name: String, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		//当任何脚本文件发生变化时清空缓存 - 应当兼容name和configGroup的变化
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedModifierElementKey) {
			val value = doResolveModifier(configGroup, name, element)
			val modificationTracker = configGroup.project.service<ParadoxModificationTrackerProvider>().Modifier
			CachedValueProvider.Result.create(value, modificationTracker)
		}
	}
	
	private fun doResolveModifier(configGroup: CwtConfigGroup, name: String, element: ParadoxScriptStringExpressionElement): ParadoxModifierElement? {
		val project = configGroup.project
		val gameType = configGroup.gameType ?: return null
		//尝试解析为预定义的非生成的修正
		val predefinedModifierConfig = configGroup.predefinedModifiers[name]
		//尝试解析为生成的修正，生成源未定义时，使用预定义的修正
		var generatedModifierConfig: CwtModifierConfig? = null
		val references = configGroup.generatedModifiers.values.firstNotNullOfOrNull { config ->
			ProgressManager.checkCanceled()
			val resolvedReferences = config.template.resolveReferences(element, name, configGroup).takeIfNotEmpty()
			if(resolvedReferences != null) generatedModifierConfig = config
			resolvedReferences
		}.orEmpty()
		if(predefinedModifierConfig == null && generatedModifierConfig == null) return null
		return ParadoxModifierElement(element, name, predefinedModifierConfig, generatedModifierConfig, project, gameType, references)
	}
	
	//TODO 检查修正的相关本地化和图标到底是如何确定的
	
	@JvmStatic
	fun getModifierNameKeys(modifierName: String, configGroup: CwtConfigGroup): List<String> {
		//mod_$, mod_country_$, ALL_UPPER_CASE is ok.
		val modifier = configGroup.modifiers[modifierName]
		val isCountryModifier = isCountryModifier(modifierName, modifier)
		return buildList {
			val nameKey = "mod_${modifierName}"
			add(nameKey)
			add(nameKey.uppercase())
			if(isCountryModifier) {
				val countryNameKey = "mod_country_${modifierName}"
				add(countryNameKey)
				add(countryNameKey.uppercase())
			}
		}
	}
	
	@JvmStatic
	fun getModifierDescKeys(modifierName: String, configGroup: CwtConfigGroup): List<String> {
		//mod_$_desc, mod_country_$_desc, ALL_UPPER_CASE is ok.
		val modifier = configGroup.modifiers[modifierName]
		val isCountryModifier = isCountryModifier(modifierName, modifier)
		return buildList {
			val descKey = "mod_${modifierName}_desc"
			add(descKey)
			add(descKey.uppercase())
			if(isCountryModifier) {
				val countryDescKey = "mod_country_${modifierName}_desc"
				add(countryDescKey)
				add(countryDescKey.uppercase())
			}
		}
	}
	
	@JvmStatic
	fun getModifierIconPaths(modifierName: String, configGroup: CwtConfigGroup): List<String> {
		//gfx/interface/icons/modifiers/mod_$.dds
		//gfx/interface/icons/modifiers/mod_country_$.dds
		val modifier = configGroup.modifiers[modifierName]
		val isCountryModifier = isCountryModifier(modifierName, modifier)
		return buildList {
			add("gfx/interface/icons/modifiers/mod_${modifierName}.dds")
			if(isCountryModifier) {
				add("gfx/interface/icons/modifiers/mod_country_${modifierName}.dds")
			}
		}
	}
	
	private fun isCountryModifier(modifierName: String, modifier: CwtModifierConfig?): Boolean {
		return (!modifierName.startsWith("country_")
			&& (modifier != null && modifier.categories.any { it.equals("country", true) || it.equals("countries", true) }))
	}
}