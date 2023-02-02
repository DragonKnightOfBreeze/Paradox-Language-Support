package icu.windea.pls.config.core

import com.intellij.codeInsight.completion.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.core.component.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
	//可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
	//修正会由特定的定义类型生成
	//对于Stellaris：修正会由经济类型（economic_category）的声明生成
	
	@JvmStatic
	fun matchesModifier(name: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		val modifierName = name.lowercase()
		//先判断是否存在对应的预定义的非生成的修正
		val predefinedModifierConfig = configGroup.predefinedModifiers[modifierName]
		if(predefinedModifierConfig != null) return true
		//否则基于解析器逻辑判断
		return ParadoxModifierResolver.matchModifier(name, configGroup, matchType)
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
		//当任何可能包含生成源的脚本文件发生变化时清空缓存 - 应当兼容name和configGroup的变化
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedModifierElementKey) {
			val value = doResolveModifier(configGroup, name, element)
			val tracker = ParadoxModificationTrackerProvider.getInstance().Modifier
			CachedValueProvider.Result.create(value, tracker)
		}
	}
	
	private fun doResolveModifier(configGroup: CwtConfigGroup, name: String, element: ParadoxScriptStringExpressionElement): ParadoxModifierElement? {
		//尝试解析为预定义的非生成的修正
		val predefinedModifierConfig = configGroup.predefinedModifiers[name]
		//尝试解析为生成的修正
		val generatedModifier = ParadoxModifierResolver.resolveModifier(name, element, configGroup)
		if(generatedModifier != null) return generatedModifier
		if(predefinedModifierConfig == null) return null
		val project = configGroup.project
		val gameType = configGroup.gameType ?: return null
		return ParadoxModifierElement(element, name, predefinedModifierConfig, gameType, project)
	}
	
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val modifiers = configGroup.modifiers
		if(modifiers.isEmpty()) return
		val element = contextElement
		if(element !is ParadoxScriptStringExpressionElement) return
		for(modifierConfig in modifiers.values) {
			//排除不匹配modifier的supported_scopes的情况
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			//首先提示生成的modifier，然后再提示预定义的modifier，排除重复的
			val tailText = CwtConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = false)
			val tailTextWithExpression = CwtConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = true)
			val template = modifierConfig.template
			if(template.isNotEmpty()) {
				//生成的modifier
				template.processResolveResult(contextElement, configGroup) { name ->
					val modifierElement = CwtConfigHandler.resolveModifier(element, name, configGroup)
					val builder = ParadoxScriptExpressionLookupElementBuilder.create(modifierElement, name)
						.withIcon(PlsIcons.Modifier)
						.withTailText(tailTextWithExpression)
						.withScopeMatched(scopeMatched)
					//.withPriority(PlsCompletionPriorities.modifierPriority)
					result.addScriptExpressionElement(context, builder)
					true
				}
			} else {
				//预定义的modifier
				val name = modifierConfig.name
				val modifierElement = CwtConfigHandler.resolveModifier(element, name, configGroup)
				val builder = ParadoxScriptExpressionLookupElementBuilder.create(modifierElement, name)
					.withIcon(PlsIcons.Modifier)
					.withTailText(tailText)
					.withScopeMatched(scopeMatched)
				//.withPriority(PlsCompletionPriorities.modifierPriority)
				result.addScriptExpressionElement(context, builder)
			}
		}
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